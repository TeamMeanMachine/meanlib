package org.team9432.annotation

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class LoggedProcessor(private val codeGenerator: CodeGenerator): SymbolProcessor {
    private val logTableType = ClassName("org.littletonrobotics.junction", "LogTable")
    private val loggableInputsType = ClassName("org.littletonrobotics.junction.inputs", "LoggableInputs")

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedClasses = resolver.getSymbolsWithAnnotation("org.team9432.annotation.Logged").filterIsInstance<KSClassDeclaration>()
        annotatedClasses.forEach { process(it) }
        return annotatedClasses.filterNot { it.validate() }.toList()
    }

    private fun process(classDeclaration: KSClassDeclaration) {
        if (!classDeclaration.modifiers.contains(Modifier.OPEN)) throw Exception("""[Logged] Please ensure the class you are annotating (${classDeclaration.simpleName.asString()}) has the open modifier!""")

        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()

        val newClassName = "Logged${className}"

        val toLogBuilder = FunSpec.builder("toLog")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("table", logTableType)
        val fromLogBuilder = FunSpec.builder("fromLog")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("table", logTableType)


        classDeclaration.getAllProperties().forEach { property ->
            val simpleName = property.simpleName.asString()
            val logName = simpleName.substring(0, 1).uppercase() + simpleName.substring(1)

            if (!property.isMutable) throw Exception("""[Logged] Please ensure the class you are annotating (${classDeclaration.simpleName.asString()}) has only mutable properties!""")

            toLogBuilder.addCode(
                """ |table.kPut("$logName", $simpleName)
                    |
                """.trimMargin()
            )

            fromLogBuilder.addCode(
                """ |$simpleName = table.kGet("$logName", $simpleName)
                    |
                """.trimMargin()
            )
        }

        val type = TypeSpec.classBuilder(newClassName)
            .addSuperinterface(loggableInputsType)
            .superclass(classDeclaration.toClassName())
            .addFunction(toLogBuilder.build())
            .addFunction(fromLogBuilder.build())


        val file = FileSpec.builder(packageName, newClassName).addType(type.build()).indent("    ").addImport("org.team9432.lib.advantagekit", listOf("kGet", "kPut")).build()
        file.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }
}

class Provider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = LoggedProcessor(
        codeGenerator = environment.codeGenerator
    )
}