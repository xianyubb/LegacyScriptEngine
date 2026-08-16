#include "lse/PluginManager.h"

#include "legacy/engine/EngineManager.h"
#include "legacy/engine/EngineOwnData.h"
#include "legacy/api/EventAPI.h"
#include "ll/api/io/FileUtils.h" // IWYU pragma: keep
#include "ll/api/mod/Mod.h"
#include "ll/api/mod/ModManager.h"
#include "ll/api/service/GamingStatus.h"
#include "ll/api/utils/ErrorUtils.h"
#include "ll/api/utils/StringUtils.h"
#include "lse/Entry.h"
#include "lse/ScriptPlugin.h"

#include <ScriptX/ScriptX.h>
#include <exception>
#include <filesystem>
#include <memory>
#include <unordered_map>

#ifdef LSE_BACKEND_LUA

constexpr auto BaseLibFileName   = "BaseLib.lua";
constexpr auto PluginManagerName = "lse-lua";

#endif

#ifdef LSE_BACKEND_QUICKJS

constexpr auto BaseLibFileName   = "BaseLib.js";
constexpr auto PluginManagerName = "lse-quickjs";

#endif

#ifdef LSE_BACKEND_PYTHON

#include "legacy/main/PythonHelper.h"
constexpr auto BaseLibFileName   = "BaseLib.py";
constexpr auto PluginManagerName = "lse-python";

#endif

#ifdef LSE_BACKEND_NODEJS

#include "legacy/main/NodeJsHelper.h"
constexpr auto PluginManagerName = "lse-nodejs";

#endif

#ifdef LSE_BACKEND_KOTLIN

#include "../../ScriptX/backend/Kotlin/KotlinEngine.h"
constexpr auto PluginManagerName = "lse-kotlin";
constexpr std::string_view KotlinApiVersion = "2";

std::unordered_map<ScriptEngine*, int64_t> compiledKotlinPluginHandles;

#endif

// Do not use legacy headers directly, otherwise there will be tons of errors.
void BindAPIs(std::shared_ptr<ScriptEngine> const& engine);
void LLSERemoveTimeTaskData(std::shared_ptr<ScriptEngine> const& engine);
bool LLSERemoveAllEventListeners(std::shared_ptr<ScriptEngine> engine);
bool LLSERemoveAllExportedFuncs(std::shared_ptr<ScriptEngine> const& engine);
bool LLSECallEventsOnHotLoad(std::shared_ptr<ScriptEngine> const& engine);
bool LLSECallEventsOnUnload(std::shared_ptr<ScriptEngine> const& engine);

namespace lse {

PluginManager::PluginManager() : ll::mod::ModManager(PluginManagerName) {}
PluginManager::~PluginManager() = default;

ll::Expected<> PluginManager::load(ll::mod::Manifest manifest) {
#ifdef LSE_BACKEND_KOTLIN
    auto& logger = lse::LegacyScriptEngine::getLogger();
    logger.info("Kotlin plugin {name}: creating script engine"_tr(fmt::arg("name", manifest.name)));
#endif
#ifdef LSE_BACKEND_PYTHON
    auto&                 logger  = lse::LegacyScriptEngine::getLogger();
    std::filesystem::path dirPath = ll::mod::getModsRoot() / manifest.name; // Plugin path
    std::string           entryPath =
        PythonHelper::findEntryScript(ll::string_utils::u8str2str(dirPath.u8string())); // Plugin entry
    // if (entryPath.empty()) return false;
    // std::string pluginName = PythonHelper::getPluginPackageName(dirPath.string()); // Plugin name

    // Run "pip install" if needed
    auto realPackageInstallDir = (std::filesystem::path(dirPath) / "site-packages").make_preferred();
    if (!std::filesystem::exists(realPackageInstallDir)) {
        std::string dependTmpFilePath =
            PythonHelper::getPluginPackDependencyFilePath(ll::string_utils::u8str2str(dirPath.u8string()));
        if (!dependTmpFilePath.empty()) {
            int exitCode = 0;
            logger.info(
                "Executing \"pip install\" for plugin {name}..."_tr(
                    fmt::arg("name", ll::string_utils::u8str2str(dirPath.filename().u8string()))
                )
            );

            if ((exitCode = PythonHelper::executePipCommand(
                     "pip install -r \"" + dependTmpFilePath + "\" -t \""
                     + ll::string_utils::u8str2str(realPackageInstallDir.u8string()) + "\" --disable-pip-version-check "
                 ))
                == 0) {
                logger.info("Pip finished successfully."_tr());
            } else logger.error("Error occurred. Exit code: {code}"_tr(fmt::arg("code", exitCode)));

            // remove temp dependency file after installation
            std::error_code ec;
            std::filesystem::remove(std::filesystem::path(dependTmpFilePath), ec);
        }
    }
#endif
#ifdef LSE_BACKEND_NODEJS
    auto&                 logger  = lse::LegacyScriptEngine::getLogger();
    std::filesystem::path dirPath = ll::mod::getModsRoot() / manifest.name; // Plugin path
    // std::string           entryPath = NodeJsHelper::findEntryScript(dirPath.string()); // Plugin entry
    // if (entryPath.empty()) return false;
    // std::string pluginName = NodeJsHelper::getPluginPackageName(dirPath.string()); // Plugin name

    // Run "npm install" if needed
    if (NodeJsHelper::doesPluginPackHasDependency(ll::string_utils::u8str2str(dirPath.u8string()))
        && !std::filesystem::exists(std::filesystem::path(dirPath) / "node_modules")) {
        int exitCode = 0;
        logger.info(
            "Executing \"npm install\" for plugin {name}..."_tr(
                fmt::arg("name", ll::string_utils::u8str2str(dirPath.filename().u8string()))
            )
        );
        if ((exitCode = NodeJsHelper::executeNpmCommand(
                 {"install", "--omit=dev", "--no-fund"},
                 ll::string_utils::u8str2str(dirPath.u8string())
             ))
            != 0) {
            logger.error("Error occurred. Exit code: {code}"_tr(fmt::arg("code", exitCode)));
        }
    }
#endif
    if (hasMod(manifest.name)) {
        return ll::makeStringError("Plugin has already loaded");
    }

    auto scriptEngine = EngineManager::newEngine(manifest.name);
#ifdef LSE_BACKEND_KOTLIN
    logger.info("Kotlin plugin {name}: script engine created"_tr(fmt::arg("name", manifest.name)));
#endif
    auto plugin       = std::make_shared<ScriptPlugin>(manifest);

    try {
        EngineScope engineScope(scriptEngine.get());

        // Init plugin logger
        getEngineOwnData()->logger = ll::io::LoggerRegistry::getInstance().getOrCreate(manifest.name);

#ifdef LSE_BACKEND_PYTHON
        scriptEngine->eval("import sys as _llse_py_sys_module");
        std::error_code ec;

        // add plugin-own site-packages to sys.path
        std::string pluginSitePackageFormatted = ll::string_utils::u8str2str(
            std::filesystem::canonical(realPackageInstallDir.make_preferred(), ec).u8string()
        );
        if (!ec) {
            scriptEngine->eval("_llse_py_sys_module.path.insert(0, r'" + pluginSitePackageFormatted + "')");
        }
        // add plugin source dir to sys.path
        std::string sourceDirFormatted =
            ll::string_utils::u8str2str(std::filesystem::canonical(dirPath.make_preferred()).u8string());
        scriptEngine->eval("_llse_py_sys_module.path.insert(0, r'" + sourceDirFormatted + "')");

        // set __file__ and __name__
        std::string entryPathFormatted = ll::string_utils::u8str2str(
            std::filesystem::canonical(std::filesystem::path(entryPath).make_preferred()).u8string()
        );
        scriptEngine->set("__file__", entryPathFormatted);
        // engine->set("__name__", String::newString("__main__"));
#endif

 #ifdef LSE_BACKEND_KOTLIN
        logger.info("Kotlin plugin {name}: binding BDS APIs"_tr(fmt::arg("name", manifest.name)));
 #endif
        BindAPIs(scriptEngine);
 #ifdef LSE_BACKEND_KOTLIN
        logger.info("Kotlin plugin {name}: BDS APIs bound"_tr(fmt::arg("name", manifest.name)));
 #endif

#if !defined(LSE_BACKEND_NODEJS) && !defined(LSE_BACKEND_KOTLIN) // NodeJs backend load depends code in another place
        auto& self = LegacyScriptEngine::getInstance().getSelf();
        // Load BaseLib.
        auto baseLibPath    = self.getModDir() / "baselib" / BaseLibFileName;
        auto baseLibContent = ll::file_utils::readFile(baseLibPath);
        if (!baseLibContent) {
            return ll::makeStringError("Failed to read BaseLib at {0}"_tr(baseLibPath.string()));
        }
        scriptEngine->eval(baseLibContent.value());
#endif
        // Load the plugin entry.
        auto entryPath             = plugin->getModDir() / manifest.entry;
        getEngineOwnData()->plugin = plugin;
#ifdef LSE_BACKEND_PYTHON
        if (!PythonHelper::loadPluginCode(
                scriptEngine,
                ll::string_utils::u8str2str(entryPath.u8string()),
                ll::string_utils::u8str2str(dirPath.u8string())
            )) {
            return ll::makeStringError("Failed to load plugin code"_tr());
        }
#endif
#ifdef LSE_BACKEND_NODEJS
        if (!NodeJsHelper::loadPluginCode(
                scriptEngine,
                ll::string_utils::u8str2str(entryPath.u8string()),
                ll::string_utils::u8str2str(dirPath.u8string()),
                NodeJsHelper::isESModulesSystem(ll::string_utils::u8str2str(dirPath.u8string()))
            )) {
            return ll::makeStringError("Failed to load plugin code"_tr());
        }
#endif
#if (defined LSE_BACKEND_QUICKJS) || (defined LSE_BACKEND_LUA)
        // Try loadFile
        try {
            scriptEngine->loadFile(entryPath.u8string());
        } catch (Exception const&) {
            // loadFile failed, try eval
            auto pluginEntryContent = ll::file_utils::readFile(entryPath);
            if (!pluginEntryContent) {
                return ll::makeStringError("Failed to read plugin entry at {0}"_tr(entryPath.string()));
            }
            scriptEngine->eval(pluginEntryContent.value(), entryPath.u8string());
        }
#endif
#ifdef LSE_BACKEND_KOTLIN
        if (entryPath.extension() == ".jar") {
            if (!manifest.extraInfo) {
                return ll::makeStringError(
                    "Compiled Kotlin plugin {0} requires extraInfo.kotlinMain and extraInfo.kotlinApiVersion"_tr(
                        manifest.name
                    )
                );
            }
            auto const& extra = manifest.extraInfo.value();
            auto mainIt       = extra.find("kotlinMain");
            auto versionIt    = extra.find("kotlinApiVersion");
            if (mainIt == extra.end() || versionIt == extra.end()) {
                return ll::makeStringError(
                    "Compiled Kotlin plugin {0} requires extraInfo.kotlinMain and extraInfo.kotlinApiVersion"_tr(
                        manifest.name
                    )
                );
            }
            if (versionIt->second != KotlinApiVersion) {
                return ll::makeStringError(
                    "Compiled Kotlin plugin {0} requests unsupported Kotlin API version {1}"_tr(
                        manifest.name,
                        versionIt->second
                    )
                );
            }

            auto* kotlinEngine = dynamic_cast<script::kotlin_backend::KotlinEngine*>(scriptEngine.get());
            if (!kotlinEngine) {
                return ll::makeStringError("Kotlin script engine is unavailable for plugin {0}"_tr(manifest.name));
            }

            logger.info("Kotlin JAR plugin {name}: loading {entry}"_tr(
                fmt::arg("name", manifest.name),
                fmt::arg("entry", entryPath.string())
            ));
            const auto handle = kotlinEngine->loadCompiledPlugin(
                ll::string_utils::u8str2str(entryPath.u8string()),
                mainIt->second,
                manifest.name
            );
            compiledKotlinPluginHandles.emplace(scriptEngine.get(), handle);
            logger.info("Kotlin JAR plugin {name}: loaded (Kotlin API v{version})"_tr(
                fmt::arg("name", manifest.name),
                fmt::arg("version", KotlinApiVersion)
            ));
        } else {
            logger.info("Kotlin plugin {name}: evaluating {entry}"_tr(
                fmt::arg("name", manifest.name),
                fmt::arg("entry", entryPath.string())
            ));
            scriptEngine->loadFile(entryPath.u8string());
            logger.info("Kotlin plugin {name}: evaluation completed"_tr(fmt::arg("name", manifest.name)));
        }
#endif
        if (ll::getGamingStatus() == ll::GamingStatus::Running) { // Is hot load
            LLSECallEventsOnHotLoad(scriptEngine);
        }
        ExitEngineScope exit;

        plugin->onDisable([this](ll::mod::Mod& self) {
            if (ll::getGamingStatus() == ll::GamingStatus::Stopping) {
                unload(self.getName());
            }
            return true;
        });

#ifdef LSE_BACKEND_KOTLIN
        // Run compiled Kotlin plugin code during the normal script plugin
        // enable callback, not during load. The Kotlin API dispatcher invokes
        // native LSE APIs on the current thread, matching JS-style semantics.
        if (auto it = compiledKotlinPluginHandles.find(scriptEngine.get());
            it != compiledKotlinPluginHandles.end()) {
            const auto handle = it->second;
            auto       engine = scriptEngine;
            const auto       pluginName = manifest.name;
            plugin->onEnable([engine, handle, pluginName](ll::mod::Mod&) {
                if (ll::getGamingStatus() == ll::GamingStatus::Stopping
                    || !EngineManager::isValid(engine.get())) {
                    return true;
                }
                try {
                    EngineScope scope(engine.get());
                    auto* kotlinEngine =
                        dynamic_cast<script::kotlin_backend::KotlinEngine*>(engine.get());
                    if (!kotlinEngine) return false;
                    kotlinEngine->enableCompiledPlugin(handle);
                    lse::LegacyScriptEngine::getLogger().info(
                        "Kotlin JAR plugin {name}: enabled"_tr(fmt::arg("name", pluginName))
                    );
                    return true;
                } catch (...) {
                    EngineScope scope(engine.get());
                    lse::LegacyScriptEngine::getLogger().error(
                        "Kotlin JAR plugin {name}: onEnable failed"_tr(fmt::arg("name", pluginName))
                    );
                    ll::error_utils::printCurrentException(lse::LegacyScriptEngine::getLogger());
                    return false;
                }
            });
        }
#endif

        auto loadResult = plugin->onLoad();
        if (!loadResult) return loadResult;

        addMod(manifest.name, plugin);
        return {};
    } catch (Exception const& e) {
        if (scriptEngine) {
            auto error = [&] {
                EngineScope engineScope(scriptEngine.get());
                return ll::makeStringError(
                    "Failed to load plugin {0}: {1}\n{2}"_tr(manifest.name, e.message(), e.stacktrace())
                );
            }();

#ifdef LSE_BACKEND_KOTLIN
            if (auto it = compiledKotlinPluginHandles.find(scriptEngine.get());
                it != compiledKotlinPluginHandles.end()) {
                static_cast<script::kotlin_backend::KotlinEngine*>(scriptEngine.get())
                    ->unloadCompiledPlugin(it->second);
                compiledKotlinPluginHandles.erase(it);
            }
#endif
#ifndef LSE_BACKEND_NODEJS
            LLSERemoveTimeTaskData(scriptEngine);
#endif
            LLSERemoveAllEventListeners(scriptEngine);
            LLSERemoveAllExportedFuncs(scriptEngine);

            EngineOwnData::clearEngineObjects(scriptEngine);
            EngineManager::unregisterEngine(scriptEngine);
#ifdef LSE_BACKEND_NODEJS
            NodeJsHelper::stopEngine(scriptEngine);
#endif
            return error;
        }
        return ll::makeStringError("Failed to load plugin {0}: {1}"_tr(manifest.name, "ScriptEngine is nullptr"));
    }
}

ll::Expected<> PluginManager::unload(std::string_view name) {
    try {
        auto scriptEngine = EngineManager::getEngine(std::string(name));

        if (!scriptEngine) {
            return ll::makeStringError("Plugin {0} not found"_tr(name));
        }

        {
            EngineScope scope(scriptEngine.get());
#ifdef LSE_BACKEND_KOTLIN
            if (auto it = compiledKotlinPluginHandles.find(scriptEngine.get());
                it != compiledKotlinPluginHandles.end()) {
                static_cast<script::kotlin_backend::KotlinEngine*>(scriptEngine.get())
                    ->unloadCompiledPlugin(it->second);
                compiledKotlinPluginHandles.erase(it);
            }
#endif
            LLSECallEventsOnUnload(scriptEngine);
#ifndef LSE_BACKEND_NODEJS
            LLSERemoveTimeTaskData(scriptEngine);
#endif
            LLSERemoveAllEventListeners(scriptEngine);
            LLSERemoveAllExportedFuncs(scriptEngine);
            EngineOwnData::clearEngineObjects(scriptEngine);
        }
        EngineManager::unregisterEngine(scriptEngine);
#ifdef LSE_BACKEND_NODEJS
        NodeJsHelper::stopEngine(scriptEngine);
#endif

        if (auto res = std::static_pointer_cast<ScriptPlugin>(getMod(name))->onUnload(); !res) {
            return res;
        }
        eraseMod(name);
        return {};
    } catch (Exception const&) {
        return ll::makeStringError("Failed to unload plugin {0}: {1}"_tr(name, "Unknown script exception"));
    } catch (std::exception const& e) {
        return ll::makeStringError("Failed to unload plugin {0}: {1}"_tr(name, e.what()));
    }
}

} // namespace lse
