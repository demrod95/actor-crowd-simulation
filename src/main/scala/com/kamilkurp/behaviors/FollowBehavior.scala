package com.kamilkurp.behaviors

import com.kamilkurp.entities.Character
import com.kamilkurp.{CharacterLeading, ControlScheme, Timer}
import org.newdawn.slick.geom.Vector2f

import scala.util.Random

class FollowBehavior extends Behavior {

  override var timer: Timer = new Timer(10000)
  var deviationTimer: Timer = new Timer(500)
  var broadcastTimer: Timer = new Timer(300)
  var deviationX: Float = 0
  var deviationY: Float = 0


  override def init(character: Character): Unit = {

  }


  def perform(character: Character, delta: Int): Unit = {
    timer.update(delta)
    deviationTimer.update(delta)
    broadcastTimer.update(delta)
    character.outOfWayTimer.update(delta)

    if (!character.lostSightOfFollowedEntity) character.lastSeenFollowedEntityTimer.update(delta)

    if (broadcastTimer.timedOut()) {
      character.room.characterList.foreach(that => {
        if (/*Math.abs(that.shape.getX - character.shape.getX) <= 1100
          && Math.abs(that.shape.getY - character.shape.getY) <= 1100
          &&*/ that != character) {
          that.actor ! CharacterLeading(character, character.shape.getCenterX, character.shape.getCenterY)
        }
      })
      broadcastTimer.reset()
    }

    if (timer.timedOut()) {
      character.setBehavior("idle")
      character.followedCharacter = null
      return
    }

    if (character.lastSeenFollowedEntityTimer.timedOut()) {
      character.lostSightOfFollowedEntity = true
      character.lastSeenFollowedEntityTimer.reset()
    }

    if (deviationTimer.timedOut()) {
      deviationX = 0.3f * Random.nextFloat() - 0.15f
      deviationY = 0.3f * Random.nextFloat() - 0.15f
      deviationTimer.reset()
    }

    val normalVector = new Vector2f(character.followX - character.shape.getCenterX, character.followY - character.shape.getCenterY)
    normalVector.normalise()

    character.walkAngle = normalVector.getTheta.floatValue()

    if (character.outOfWayTimer.timedOut()) {
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

      character.setBehavior("holdMeetPoint")
    }


  }

}

