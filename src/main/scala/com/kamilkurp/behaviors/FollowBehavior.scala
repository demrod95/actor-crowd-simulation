package com.kamilkurp.behaviors

import com.kamilkurp.entities.Character
import com.kamilkurp.{CharacterLeading, ControlScheme}
import org.newdawn.slick.geom.Vector2f

import scala.util.Random

class FollowBehavior extends Behavior {

  override var timer: Int = 0
  override var timerTimeout: Int = 10000
  val deviationTimerTimeout: Int = 500
  val broadcastTimerTimeout: Int = 300
  var deviationX: Float = 0
  var deviationY: Float = 0
  var deviationTimer: Int = 0
  var broadcastTimer: Int = 0



  def perform(character: Character, delta: Int): Unit = {
    timer += delta
    deviationTimer += delta
    character.outOfWayTimer += delta
    if (!character.lostSightOfFollowedEntity) character.lastSeenFollowedEntityTimer += delta

    if (broadcastTimer > broadcastTimerTimeout) {
      character.room.characterList.foreach(that => {
        if (/*Math.abs(that.shape.getX - character.shape.getX) <= 1100
          && Math.abs(that.shape.getY - character.shape.getY) <= 1100
          &&*/ that != character) {
          that.actor ! CharacterLeading(character, character.shape.getCenterX, character.shape.getCenterY)
        }
      })
      broadcastTimer = 0
    }

    if (timer > timerTimeout) {
      character.currentBehavior = "idle"
      character.followedCharacter = null
      return
    }

    if (character.lastSeenFollowedEntityTimer > character.lastSeenFollowedEntityTimerTimeout) {
      character.lostSightOfFollowedEntity = true
      character.lastSeenFollowedEntityTimer = 0
    }

    if (deviationTimer > deviationTimerTimeout) {
      deviationX = 0.3f * Random.nextFloat() - 0.15f
      deviationY = 0.3f * Random.nextFloat() - 0.15f
      deviationTimer = 0
    }

    val normalVector = new Vector2f(character.followX - character.shape.getCenterX, character.followY - character.shape.getCenterY)
    normalVector.normalise()

    character.walkAngle = normalVector.getTheta.floatValue()

    if (character.outOfWayTimer > character.outOfWayTimerTimeout) {
      character.movingOutOfTheWay = false
      if (character.controlScheme != ControlScheme.Manual) {
        if (character.getDistanceTo(character.followX, character.followY) > character.followDistance) {

          character.currentVelocityX = (normalVector.x + deviationX) * character.speed * (1f - character.slow)
          character.currentVelocityY = (normalVector.y + deviationY) * character.speed * (1f - character.slow)
        }
        else {
          character.currentVelocityX = 0
          character.currentVelocityY = 0
        }

      }
    }

    if (character.room.meetPointList.nonEmpty) {
      character.followX = character.room.meetPointList.head.shape.getCenterX
      character.followY = character.room.meetPointList.head.shape.getCenterY

      character.currentBehavior = "holdMeetPoint"
    }


  }
}

