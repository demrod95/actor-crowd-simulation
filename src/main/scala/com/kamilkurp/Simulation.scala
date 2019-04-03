package com.kamilkurp

import akka.actor.{ActorRef, ActorSystem, Props}
import com.kamilkurp.entities.{Door, MeetPoint}
import org.newdawn.slick._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

object CameraView {
  var x: Float = 0.0f
  var y: Float = 0.0f
}

class Simulation(gameName: String) extends BasicGame(gameName) {
  val system: ActorSystem = ActorSystem("crowd_sim_system")
  val numberOfAgents: Int = 100
  val addManualAgent: Boolean = true

  var listOfNames = Array("Virgil", "Dominique", "Hermina",
    "Carolynn", "Adina", "Elida", "Classie", "Raymonde",
    "Lovie", "Theola", "Damion", "Petronila", "Corrinne",
    "Arica", "Alfonso", "Madalene", "Alvina", "Eliana", "Jarrod", "Thora")
  val nameIndeces: mutable.Map[String, Int] = mutable.Map[String, Int]()

  var doorImage: Image = _
  var characterImage: Image = _

  var roomList: ListBuffer[Room] = new ListBuffer[Room]
  var doorList: ListBuffer[Door] = new ListBuffer[Door]

  var officeList: ListBuffer[Room] = new ListBuffer[Room]

  var mutableActorList = new ListBuffer[ActorRef]()


  var actorList: List[ActorRef] = mutableActorList.toList

  var renderScale: Float = 1.5f

  override def init(gc: GameContainer): Unit = {
    doorImage = new Image("door.png")
    characterImage = new Image("character.png")

    val filename = "building.txt"
    for (line <- Source.fromFile(filename).getLines) {
      if (!line.isEmpty) {
        var split: Array[String] = line.split(" ")

        if (split(0) == "room") {
          val room = new Room(split(1), split(2).toInt, split(3).toInt, split(4).toInt, split(5).toInt)
          roomList += room
          if (room.name.startsWith("room")) officeList += room

        }
        else if (split(0) == "door") {
          var room: Room = null
          var linkToDoor: Door = null

          if (split.length >= 6) {
            roomList.foreach(that => {
              if (that.name == split(5)) room = that
            })
          }
          if (split.length >= 7) {
            doorList.foreach(that => {
              if (that.name == split(6)) linkToDoor = that
            })
          }
          val door = new Door(split(1), room, split(3).toInt, split(4).toInt, doorImage)
          if (linkToDoor != null) door.connectWith(linkToDoor)
          if (split(2) == "1") {
            room.evacuationDoor = door
          }
          doorList += door
        }
        else if (split(0) == "meet") {
          var room: Room = null

          if (split.length >= 5) {
            roomList.foreach(that => {
              if (that.name == split(4)) room = that
            })
          }

          val meetPoint = new MeetPoint(split(1), room, split(2).toInt, split(3).toInt)
          room.meetPointList += meetPoint
        }
      }
    }

    val roomsFiltered = roomList.filter(room => room.name == "roomA")
    val room1 = if (roomsFiltered.nonEmpty) roomsFiltered.head else null

    for (name <- listOfNames) {
      nameIndeces.put(name, 0)
    }

    for (_ <- 0 until numberOfAgents) {

      val room: Room = officeList(Random.nextInt(officeList.length))

      val randomNameIndex = Random.nextInt(listOfNames.length)
      val randomName = listOfNames(randomNameIndex) + nameIndeces(listOfNames(randomNameIndex))
      nameIndeces.put(listOfNames(randomNameIndex), nameIndeces(listOfNames(randomNameIndex)) + 1)

      val character = new entities.Character(randomName, room, ControlScheme.Agent, characterImage)
      room.characterList += character
      val actor = system.actorOf(Props(new CharacterActor(randomName, character)))
      mutableActorList += actor

      character.setActor(actor)
    }


    if (addManualAgent) {
      val playerName = "Player"
      val character = new entities.Character(playerName, room1, ControlScheme.Manual, (Input.KEY_A, Input.KEY_D, Input.KEY_W, Input.KEY_S), characterImage)

      val actor = system.actorOf(Props(new CharacterActor("Player", character)))

      character.setActor(actor)

      room1.characterList += character
    }
  }

  override def update(gc: GameContainer, i: Int): Unit = {

    if (gc.getInput.isKeyDown(Input.KEY_DOWN)) {
      CameraView.y = CameraView.y + (1.0f * i.toFloat)
    }
    if (gc.getInput.isKeyDown(Input.KEY_UP)) {
      CameraView.y = CameraView.y - (1.0f * i.toFloat)
    }
    if (gc.getInput.isKeyDown(Input.KEY_RIGHT)) {
      CameraView.x = CameraView.x + (1.0f * i.toFloat)
    }
    if (gc.getInput.isKeyDown(Input.KEY_LEFT)) {
      CameraView.x = CameraView.x - (1.0f * i.toFloat)
    }

    if (gc.getInput.isKeyDown(Input.KEY_SUBTRACT)) {
      val centerX = CameraView.x + Globals.WINDOW_X * 1/renderScale / 2
      val centerY = CameraView.y + Globals.WINDOW_Y * 1/renderScale / 2
      renderScale /= 1 + 0.005f
      CameraView.x = centerX - (Globals.WINDOW_X * 1/renderScale / 2)
      CameraView.y = centerY - (Globals.WINDOW_Y * 1/renderScale / 2)

    }
    if (gc.getInput.isKeyDown(Input.KEY_ADD)) {
      val centerX = CameraView.x + Globals.WINDOW_X * 1/renderScale / 2
      val centerY = CameraView.y + Globals.WINDOW_Y * 1/renderScale / 2
      renderScale *= 1 + 0.005f
      CameraView.x = centerX - (Globals.WINDOW_X * 1/renderScale / 2)
      CameraView.y = centerY - (Globals.WINDOW_Y * 1/renderScale / 2)
    }


    roomList.foreach(room => {
      room.update(gc, i, renderScale)
    })
  }


  override def render(gc: GameContainer, g: Graphics): Unit = {
    g.scale(renderScale, renderScale)
    roomList.foreach(room => {

      room.render(g, doorImage, CameraView.x, CameraView.y)
    })
  }
}
