// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
package org.team2471.frc.lib.framework.internal.akitLoggers

import edu.wpi.first.units.Measure
import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.struct.StructSerializable
import edu.wpi.first.wpilibj.DriverStation
import org.littletonrobotics.junction.AutoLogOutput
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.*

object KAutoLogOutputManager {
    private val callbacks: MutableList<Runnable> = ArrayList()
    private val scannedObjectHashes: MutableList<Int> = ArrayList()
    private val allowedPackages: MutableSet<String> = HashSet()

    /**
     * Adds a new allowed package to use when scanning for annotations. By default,
     * the parent class where `@AutoLogOutput` is used must be within the same
     * package as `Robot` (or a subpackage). Calling this method registers a
     * new allowed package, such as a "lib" package outside of normal robot code.
     *
     *
     *
     * This method must be called within the constructor of `Robot`.
     *
     * @param packageName The new allowed package name (e.g. "frc.lib")
     */
    fun addPackage(packageName: String) {
        allowedPackages.add(packageName)
    }

    /** Records values from all registered fields.  */
    fun periodic() {
        for (callback in callbacks) {
            callback.run()
        }
    }

    /**
     * Registers a root object, scanning for loggable fields recursively.
     *
     * @param root The object to scan recursively.
     */
    fun addObject(root: Any) {
        allowedPackages.add(root.javaClass.packageName)
        addObjectImpl(root)
    }

    /**
     * Registers a root object, scanning for loggable fields recursively.
     *
     * @param root The object to scan recursively.
     */
    private fun addObjectImpl(root: Any) {
        // Check if package name is valid
        val packageName = root.javaClass.packageName
        var packageNameValid = false
        for (allowedPackage in allowedPackages) {
            if (packageName.startsWith(allowedPackage)) {
                packageNameValid = true
                break
            }
        }
        if (!packageNameValid) return

        // Check if object has already been scanned
        if (scannedObjectHashes.contains(root.hashCode())) return
        scannedObjectHashes.add(root.hashCode())

        // If array, loop over individual items
        if (root.javaClass.isArray) {
            val rootArray = root as Array<Any>
            for (item in rootArray) {
                if (item != null) {
                    addObjectImpl(item)
                }
            }
            return
        }

        // Loop over declared methods
        getAllMethods(root.javaClass).forEach(Consumer<MethodAndDeclaringClass> { methodAndDeclaringClass: MethodAndDeclaringClass ->
            val method = methodAndDeclaringClass.method
            val declaringClass = methodAndDeclaringClass.declaringClass
            if (!method.trySetAccessible()) return@Consumer

            // If annotated, try to add
            if (method.isAnnotationPresent(AutoLogOutput::class.java)) {
                // Exit if invalid signature
                if (method.returnType == Void.TYPE
                    || method.parameterCount > 0 || method.exceptionTypes.size > 0
                ) {
                    return@Consumer
                }

                // Get key
                val keyParameter = method.getAnnotation(AutoLogOutput::class.java).key
                val key = makeKey(keyParameter, method.name, declaringClass, root)

                // Register method
                registerField(
                    key,
                    method.returnType
                ) {
                    try {
                        return@registerField method.invoke(root)
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                        return@registerField null
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        return@registerField null
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                        return@registerField null
                    }
                }
            }
        })

        // Loop over declared fields
        getAllFields(root.javaClass).forEach(Consumer<FieldAndDeclaringClass> { fieldAndDeclaringClass: FieldAndDeclaringClass ->
            val field = fieldAndDeclaringClass.field
            val declaringClass = fieldAndDeclaringClass.declaringClass
            if (!field.trySetAccessible()) return@Consumer

            // If annotated, try to add
            if (field.isAnnotationPresent(AutoLogOutput::class.java)) {
                // Get key
                val keyParameter = field.getAnnotation(AutoLogOutput::class.java).key
                val key = makeKey(keyParameter, field.name, declaringClass, root)

                // Register field
                registerField(
                    key,
                    field.type
                ) {
                    try {
                        return@registerField field[root]
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        return@registerField null
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                        return@registerField null
                    }
                }
                return@Consumer
            }

            // Scan field value
            var fieldValue: Any? = null
            try {
                fieldValue = field[root]
            } catch (e: IllegalArgumentException) {
                return@Consumer
            } catch (e: IllegalAccessException) {
                return@Consumer
            }
            if (fieldValue != null) {
                addObjectImpl(fieldValue)
            }
        })
    }

    /**
     * Returns the set of all methods on the class and its superclasses (public and
     * private).
     */
    private fun getAllMethods(type: Class<*>?): List<MethodAndDeclaringClass> {
        var type = type
        val methods: MutableList<MethodAndDeclaringClass> = ArrayList()
        while (type != null && type != Any::class.java) {
            for (method in type.declaredMethods) {
                methods.add(MethodAndDeclaringClass(method, type))
            }
            type = type.superclass
        }
        return methods
    }

    /**
     * Returns the set of all fields in the class and its superclasses (public and
     * private).
     */
    private fun getAllFields(type: Class<*>?): List<FieldAndDeclaringClass> {
        var type = type
        val fields: MutableList<FieldAndDeclaringClass> = ArrayList()
        while (type != null && type != Any::class.java) {
            for (field in type.declaredFields) {
                fields.add(FieldAndDeclaringClass(field, type))
            }
            type = type.superclass
        }
        return fields
    }

    /**
     * Finds the field in the provided class and its superclasses (must be public or
     * protected in superclasses). Returns null if the field cannot be found.
     */
    private fun findField(type: Class<*>?, fieldName: String): Field? {
        var type = type
        try {
            return type!!.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            // Not in original class, check superclasses
            type = type!!.superclass
            while (type != null && type != Any::class.java) {
                try {
                    val field = type.getDeclaredField(fieldName)
                    if (Modifier.isPublic(field.modifiers) || Modifier.isProtected(field.modifiers)) {
                        return field
                    }
                } catch (e1: NoSuchFieldException) {
                } catch (e1: SecurityException) {
                }
                type = type.superclass
            }
            return null
        } catch (e: SecurityException) {
            return null
        }
    }

    /**
     * Generates a log key based on the field properties.
     *
     * @param keyParameter   The user-provided key from the annotation
     * @param valueName      The name of the field or method
     * @param declaringClass The class where this fields or method is declared
     * @param parent         The parent object to read data from
     */
    private fun makeKey(keyParameter: String, valueName: String, declaringClass: Class<*>, parent: Any): String {
        var valueName = valueName
        if (keyParameter.length == 0) {
            // Auto generate from parent and value
            var key = declaringClass.simpleName + "/"
            if (valueName.startsWith("get") && valueName.length > 3) {
                valueName = valueName.substring(3)
            }
            key += valueName.substring(0, 1).uppercase(Locale.getDefault()) + valueName.substring(1)
            return key
        } else {
            // Fill in field values
            var key = keyParameter
            while (true) {
                // Find field name
                val openIndex = key.indexOf("{")
                if (openIndex == -1) break // No more brackets

                val closeIndex = key.indexOf("}", openIndex)
                if (closeIndex == -1) break // No closing bracket

                val fieldName = key.substring(openIndex + 1, closeIndex)

                // Get field value
                var fieldValue = ""
                val field: Field?
                try {
                    field = findField(declaringClass, fieldName)
                    field!!.isAccessible = true
                    fieldValue = field[parent].toString()
                } catch (e: SecurityException) {
                    // Use default field value
                } catch (e: IllegalArgumentException) {
                } catch (e: IllegalAccessException) {
                } catch (e: NullPointerException) {
                }

                // Replace in key
                key = key.substring(0, openIndex) + fieldValue + key.substring(closeIndex + 1)
            }
            return key
        }
    }

    /**
     * Registers the periodic callback for a single field.
     *
     * @param key      The string key to use for logging.
     * @param type     The type of object being logged.
     * @param supplier A supplier for the field values.
     */
    private fun registerField(key: String, type: Class<*>, supplier: Supplier<*>) {
        if (!type.isArray) {
            // Single types
            if (type == Boolean::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Boolean)
                    })
            } else if (type == Int::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Int)
                    })
            } else if (type == Long::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Long)
                    })
            } else if (type == Float::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Float)
                    })
            } else if (type == Double::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Double)
                    })
            } else if (type == String::class.java) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as String)
                    })
            } else if (type.isEnum) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null)  // Cannot cast to enum subclass, log the name directly
                            Logger.recordOutput(key, (value as Enum<*>).name)
                    })
            } else if (BooleanSupplier::class.java.isAssignableFrom(type)) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as BooleanSupplier)
                    })
            } else if (IntSupplier::class.java.isAssignableFrom(type)) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as IntSupplier)
                    })
            } else if (LongSupplier::class.java.isAssignableFrom(type)) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as LongSupplier)
                    })
            } else if (DoubleSupplier::class.java.isAssignableFrom(type)) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as DoubleSupplier)
                    })
            } else if (Measure::class.java.isAssignableFrom(type)) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Measure<*>)
                    })
            } else if (type == LoggedMechanism2d::class.java) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as LoggedMechanism2d)
                    })
            } else if (type.isRecord) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Record)
                    })
            } else {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) try {
                            Logger.recordOutput(key, value as WPISerializable)
                        } catch (e: ClassCastException) {
                            DriverStation.reportError(
                                "[AdvantageKit] Auto serialization is not supported for type " + type.simpleName,
                                false
                            )
                        }
                    })
            }
        } else if (!type.componentType.isArray) {
            // Array types
            val componentType = type.componentType
            if (componentType == Byte::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as ByteArray)
                    })
            } else if (componentType == Boolean::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as BooleanArray)
                    })
            } else if (componentType == Int::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as IntArray)
                    })
            } else if (componentType == Long::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as LongArray)
                    })
            } else if (componentType == Float::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as FloatArray)
                    })
            } else if (componentType == Double::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as DoubleArray)
                    })
            } else if (componentType == String::class.java) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<String?>)
                    })
            } else if (componentType.isEnum) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) {
                            // Cannot cast to enum subclass, log the names directly
                            val enumValue = value as Array<Enum<*>>
                            val names = arrayOfNulls<String>(enumValue.size)
                            for (i in enumValue.indices) {
                                names[i] = enumValue[i].name
                            }
                            Logger.recordOutput(key, names)
                        }
                    })
            } else if (componentType.isRecord) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, *value as Array<Record?>)
                    })
            } else {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) {
                            try {
                                Logger.recordOutput(key, *value as Array<StructSerializable?>)
                            } catch (e: ClassCastException) {
                                DriverStation.reportError(
                                    "[AdvantageKit] Auto serialization is not supported for array type "
                                            + componentType.simpleName,
                                    false
                                )
                            }
                        }
                    })
            }
        } else {
            // 2D array types
            val componentType = type.componentType.componentType
            if (componentType == Byte::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<ByteArray?>)
                    })
            } else if (componentType == Boolean::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<BooleanArray?>)
                    })
            } else if (componentType == Int::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<IntArray?>)
                    })
            } else if (componentType == Long::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<LongArray?>)
                    })
            } else if (componentType == Float::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<FloatArray?>)
                    })
            } else if (componentType == Double::class.javaPrimitiveType) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<DoubleArray?>)
                    })
            } else if (componentType == String::class.java) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<Array<String?>?>)
                    })
            } else if (componentType.isEnum) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) {
                            // Cannot cast to enum subclass, log the names directly
                            val enumValue = value as Array<Array<Enum<*>>>
                            val names: Array<Array<String?>?> = arrayOfNulls(enumValue.size)
                            for (row in enumValue.indices) {
                                val rowValue = enumValue[row]
                                names[row] = arrayOfNulls(rowValue.size)
                                for (column in rowValue.indices) {
                                    names[row]?.set(column, rowValue[column].name)
                                }
                            }
                            Logger.recordOutput(key, names)
                        }
                    })
            } else if (componentType.isRecord) {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) Logger.recordOutput(key, value as Array<Array<Record>?>)
                    })
            } else {
                callbacks.add(
                    Runnable {
                        val value = supplier.get()
                        if (value != null) {
                            try {
                                Logger.recordOutput(key, value as Array<Array<StructSerializable>?>)
                            } catch (e: ClassCastException) {
                                DriverStation.reportError(
                                    "[AdvantageKit] Auto serialization is not supported for 2D array type "
                                            + componentType.simpleName,
                                    false
                                )
                            }
                        }
                    })
            }
        }
    }

    private class MethodAndDeclaringClass(val method: Method, val declaringClass: Class<*>)

    private class FieldAndDeclaringClass(val field: Field, val declaringClass: Class<*>)
}