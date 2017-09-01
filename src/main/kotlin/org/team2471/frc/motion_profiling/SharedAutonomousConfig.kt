package org.team2471.frc.motion_profiling

import edu.wpi.first.wpilibj.networktables.NetworkTable
import edu.wpi.first.wpilibj.tables.ITable
import org.team2471.frc.lib.motion_profiling.MotionKey
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.Path2DPoint

private fun Path2DPoint.remainingPoints() : List<Path2DPoint> {
  val result = ArrayList<Path2DPoint>()

  var currentPoint = this
  while (true) {
    result.add(currentPoint)
    currentPoint = currentPoint.nextPoint
    if(currentPoint == null) break
  }

  return result
}

private fun MotionKey.remainingKeys() : List<MotionKey> {
  val result = ArrayList<MotionKey>()

  var currentKey = this
  while (true) {
    result.add(currentKey)
    currentKey = currentKey.nextKey
    if(currentKey == null) break
  }

  return result
}

private fun Path2D.dumpToTable(table: ITable) {
  table.putStringArray("XyCurvePoints", when (this.xyCurve.headPoint) {
    null -> arrayOf() // empty array
    else -> this.xyCurve.headPoint.remainingPoints().map {
      point -> point.toString()
    }.toTypedArray()
  })

  table.putStringArray("EaseCurveKeys", when (this.easeCurve.headKey) {
    null -> arrayOf() // empty array
    else -> this.easeCurve.headKey.remainingKeys().map {
      point -> point.toString()
    }.toTypedArray()
  })
}


class SharedAutonomousConfig(val name: String) {

  private val paths: MutableMap<String, Path2D> = HashMap()
  private val table = NetworkTable.getTable("PathVisualizer").getSubTable("Config_" + name)

  fun putPath(pathName: String, path: Path2D) = paths.put(pathName, path)

  fun getPath(pathName: String): Path2D? = paths[pathName]

  fun deletePath(pathName: String) = paths.remove(pathName)

  fun renamePath(prevPathName: String, newPathName: String) = paths.put(newPathName, paths.remove(prevPathName)!!)

  fun updatePath(pathName: String) {
    val path = getPath(pathName)!!
    val pathTable = table.getSubTable("Path_" + pathName)
    path.dumpToTable(pathTable)
  }
}