param(
    [string]$SourceRoot = (Join-Path $PSScriptRoot '..\src\legacy\api'),
    [string]$OutputFile = (Join-Path $PSScriptRoot '..\src-server\lse\kotlin-api\org\legacy\scriptengine\kotlin\api\GeneratedLseApi.kt')
)

$ErrorActionPreference = 'Stop'

$keywords = @(
    'as', 'break', 'class', 'continue', 'do', 'else', 'false', 'for', 'fun',
    'if', 'in', 'interface', 'is', 'null', 'object', 'package', 'return',
    'super', 'this', 'throw', 'true', 'try', 'typealias', 'typeof', 'val',
    'var', 'when', 'while'
)

function ConvertTo-KotlinIdentifier([string]$name) {
    if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$' -and $keywords -notcontains $name) {
        return $name
    }
    return "``$name``"
}

$definitions = @{}
Get-ChildItem -LiteralPath $SourceRoot -Filter '*.cpp' -File | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $matches = [regex]::Matches($text, 'defineClass(?:<[^>]+>)?\("(?<name>[^"]+)"\)')
    foreach ($match in $matches) {
        $buildEnd = $text.IndexOf('.build();', $match.Index)
        if ($buildEnd -lt 0) { continue }
        $chunkLength = $buildEnd + 9 - $match.Index
        $chunk = $text.Substring($match.Index, $chunkLength)
        $chunk = [regex]::Replace($chunk, '(?m)//.*$', '')
        $name = $match.Groups['name'].Value
        if (-not $definitions.ContainsKey($name)) {
            $definitions[$name] = [ordered]@{
                Name = $name
                StaticFunctions = [System.Collections.Generic.HashSet[string]]::new()
                StaticProperties = [System.Collections.Generic.HashSet[string]]::new()
                InstanceFunctions = [System.Collections.Generic.HashSet[string]]::new()
                InstanceProperties = [System.Collections.Generic.HashSet[string]]::new()
                Constructor = $false
            }
        }
        $definition = $definitions[$name]
        $constructor = [regex]::Match($chunk, '\.constructor\((?<body>[^)]*)\)')
        if ($constructor.Success -and $constructor.Groups['body'].Value.Trim() -ne 'nullptr') {
            $definition.Constructor = $true
        }
        foreach ($member in [regex]::Matches(
            $chunk,
            '\.(?<kind>function|instanceFunction|property|instanceProperty)\("(?<name>[A-Za-z_][A-Za-z0-9_]*)"'
        )) {
            switch ($member.Groups['kind'].Value) {
                'function' { [void]$definition.StaticFunctions.Add($member.Groups['name'].Value) }
                'property' { [void]$definition.StaticProperties.Add($member.Groups['name'].Value) }
                'instanceFunction' { [void]$definition.InstanceFunctions.Add($member.Groups['name'].Value) }
                'instanceProperty' { [void]$definition.InstanceProperties.Add($member.Groups['name'].Value) }
            }
        }
    }
}

function Add-Line([System.Text.StringBuilder]$builder, [string]$line = '') {
    [void]$builder.AppendLine($line)
}

$builder = [System.Text.StringBuilder]::new()
Add-Line $builder 'package org.legacy.scriptengine.kotlin.api'
Add-Line $builder
Add-Line $builder 'import org.legacy.scriptengine.kotlin.runtime.ScriptXKotlinRuntime'
Add-Line $builder 'import kotlin.jvm.JvmName'
Add-Line $builder
Add-Line $builder 'internal interface LseNativeBacked {'
Add-Line $builder '    val rawNativeValue: Any?'
Add-Line $builder '}'
Add-Line $builder
Add-Line $builder 'internal object LseKotlinApiRuntime {'
Add-Line $builder '    private fun unwrap(value: Any?): Any? ='
Add-Line $builder '        (value as? LseNativeBacked)?.rawNativeValue ?: value'
Add-Line $builder
Add-Line $builder '    private fun unwrapArgs(args: Array<out Any?>): Array<Any?> ='
Add-Line $builder '        args.map(::unwrap).toTypedArray()'
Add-Line $builder
Add-Line $builder '    fun global(name: String, vararg args: Any?): Any? ='
Add-Line $builder '        ScriptXKotlinRuntime.call(name, *unwrapArgs(args)).wrap()'
Add-Line $builder
Add-Line $builder '    fun static(className: String, name: String, vararg args: Any?): Any? ='
Add-Line $builder '        ScriptXKotlinRuntime.call("__scriptx_api_${className}_s_$name", *unwrapArgs(args)).wrap()'
Add-Line $builder
Add-Line $builder '    fun instance(className: String, name: String, receiver: Any?, vararg args: Any?): Any? ='
Add-Line $builder '        ScriptXKotlinRuntime.callInstance(className, name, unwrap(receiver), *unwrapArgs(args)).wrap()'
Add-Line $builder
Add-Line $builder '    fun property(className: String, name: String, receiver: Any?): Any? ='
Add-Line $builder '        instance(className, "${name}_get", receiver)'
Add-Line $builder
Add-Line $builder '    fun setProperty(className: String, name: String, receiver: Any?, value: Any?) {'
Add-Line $builder '        instance(className, "${name}_set", receiver, value)'
Add-Line $builder '    }'
Add-Line $builder
Add-Line $builder '    fun construct(className: String, vararg args: Any?): Any? ='
Add-Line $builder '        ScriptXKotlinRuntime.construct(className, *unwrapArgs(args)).wrap()'
Add-Line $builder
Add-Line $builder '    internal fun wrapNative(value: Any?): Any? = value.wrap()'
Add-Line $builder
Add-Line $builder '    private class ListenerAdapter('
Add-Line $builder '        private val callback: Any,'
Add-Line $builder '        private val dispatcher: ScriptXKotlinRuntime.Dispatcher'
Add-Line $builder '    ) : Function<Any?> {'
Add-Line $builder '        fun invoke(args: Array<Any?>): Any? {'
Add-Line $builder '            return ScriptXKotlinRuntime.withDispatcher(dispatcher) {'
Add-Line $builder '                val wrapped = args.map { it.wrap() }.toTypedArray()'
Add-Line $builder '                val method = callback.javaClass.methods.firstOrNull {'
Add-Line $builder '                    it.name == "invoke" && it.parameterCount == wrapped.size'
Add-Line $builder '                } ?: error("Kotlin mc.listen callback arity is unsupported: ${wrapped.size}")'
Add-Line $builder '                method.isAccessible = true'
Add-Line $builder '                method.invoke(callback, *wrapped)'
Add-Line $builder '            }'
Add-Line $builder '        }'
Add-Line $builder '    }'
Add-Line $builder
Add-Line $builder '    fun prepareListenerArgs(args: Array<out Any?>): Array<Any?> {'
Add-Line $builder '        val copy = Array<Any?>(args.size) { args[it] }'
Add-Line $builder '        if (args.size < 2 || args[1] !is Function<*>) return copy'
Add-Line $builder '        if (args[1]!!.javaClass.name == "ScriptXKotlinHost\$NativeFunction") return copy'
Add-Line $builder '        copy[1] = ListenerAdapter(args[1]!!, ScriptXKotlinRuntime.capture())'
Add-Line $builder '        return copy'
Add-Line $builder '    }'
Add-Line $builder
Add-Line $builder '    private fun Any?.wrap(): Any? {'
Add-Line $builder '        if (this == null || this is LseNativeBacked) return this'
Add-Line $builder '        return when (ScriptXKotlinRuntime.nativeClassName(this)) {'
foreach ($definition in $definitions.Values) {
    if ($definition.InstanceFunctions.Count -gt 0 -or
        $definition.InstanceProperties.Count -gt 0 -or
        $definition.Constructor) {
        $className = ConvertTo-KotlinIdentifier $definition.Name
        Add-Line $builder "            `"$($definition.Name)`" -> $className(this)"
    }
}
Add-Line $builder '            else -> this'
Add-Line $builder '        }'
Add-Line $builder '    }'
Add-Line $builder '}'
Add-Line $builder

$globalFunctions = @('log', 'colorLog', 'fastLog', 'setTimeout', 'setInterval', 'clearInterval')
foreach ($name in $globalFunctions) {
    $identifier = ConvertTo-KotlinIdentifier $name
    Add-Line $builder "fun $identifier(vararg args: Any?): Any? = LseKotlinApiRuntime.global(`"$name`", *args)"
}
Add-Line $builder

foreach ($definition in $definitions.Values | Sort-Object Name) {
    $className = ConvertTo-KotlinIdentifier $definition.Name
    $hasInstance = $definition.InstanceFunctions.Count -gt 0 -or
        $definition.InstanceProperties.Count -gt 0 -or $definition.Constructor
    if ($hasInstance) {
        Add-Line $builder "open class $className internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {"
        if ($definition.Constructor) {
            Add-Line $builder '    companion object {'
            Add-Line $builder "        operator fun invoke(vararg args: Any?): $className ="
            Add-Line $builder "            LseKotlinApiRuntime.construct(`"$($definition.Name)`", *args) as $className"
            Add-Line $builder '    }'
        }
        foreach ($name in $definition.InstanceFunctions | Sort-Object) {
            $identifier = ConvertTo-KotlinIdentifier $name
            Add-Line $builder "    fun $identifier(vararg args: Any?): Any? ="
            Add-Line $builder "        LseKotlinApiRuntime.instance(`"$($definition.Name)`", `"$name`", this, *args)"
        }
        foreach ($name in $definition.InstanceProperties | Sort-Object) {
            $identifier = ConvertTo-KotlinIdentifier $name
            $jvmSafeName = $name -replace '[^A-Za-z0-9_]', '_'
            Add-Line $builder "    @get:JvmName(`"get_${jvmSafeName}`")"
            Add-Line $builder "    @set:JvmName(`"set_${jvmSafeName}`")"
            Add-Line $builder "    var ${identifier}: Any?"
            Add-Line $builder "        get() = LseKotlinApiRuntime.property(`"$($definition.Name)`", `"$name`", this)"
            Add-Line $builder "        set(value) { LseKotlinApiRuntime.setProperty(`"$($definition.Name)`", `"$name`", this, value) }"
        }
        Add-Line $builder '}'
    } else {
        Add-Line $builder "object $className {"
        foreach ($name in $definition.StaticFunctions | Sort-Object) {
            $identifier = ConvertTo-KotlinIdentifier $name
            Add-Line $builder "    fun $identifier(vararg args: Any?): Any? ="
            if ($definition.Name -eq 'mc' -and $name -eq 'listen') {
                Add-Line $builder "        LseKotlinApiRuntime.static(`"$($definition.Name)`", `"$name`", *LseKotlinApiRuntime.prepareListenerArgs(args))"
            } else {
                Add-Line $builder "        LseKotlinApiRuntime.static(`"$($definition.Name)`", `"$name`", *args)"
            }
        }
        foreach ($name in $definition.StaticProperties | Sort-Object) {
            $identifier = ConvertTo-KotlinIdentifier $name
            $jvmSafeName = $name -replace '[^A-Za-z0-9_]', '_'
            Add-Line $builder "    @get:JvmName(`"get_${jvmSafeName}`")"
            Add-Line $builder "    @set:JvmName(`"set_${jvmSafeName}`")"
            Add-Line $builder "    var ${identifier}: Any?"
            Add-Line $builder "        get() = LseKotlinApiRuntime.static(`"$($definition.Name)`", `"$name`_get`")"
            Add-Line $builder "        set(value) { LseKotlinApiRuntime.static(`"$($definition.Name)`", `"$name`_set`", value) }"
        }
        Add-Line $builder '}'
    }
    Add-Line $builder
}

$parent = Split-Path -Parent $OutputFile
New-Item -ItemType Directory -Path $parent -Force | Out-Null
[System.IO.File]::WriteAllText($OutputFile, $builder.ToString(), [Text.Encoding]::UTF8)
Write-Output "Generated $($definitions.Count) Kotlin LSE API classes at $OutputFile"
