package com.kamilkurp.entities

import com.kamilkurp.Room
import org.newdawn.slick.Image
import org.newdawn.slick.geom.{Rectangle, Shape}

abstract class Entity() {
  val name: String
//  var x: Float
//  var y: Float
//  var w: Float
//  var h: Float
  var currentVelocityX: Float
  var currentVelocityY: Float
  var room: Room
  var image: Image

  var shape: Shape

  def onCollision(entity: Entity)

  def changeRoom(room: Room, newX: Float, newY: Float)
}
