package frc.team5190.livedashboard

import javafx.collections.FXCollections
import javafx.scene.text.FontWeight
import tornadofx.*
import tornadofx.Stylesheet.Companion.comboBox

class LiveDashboardApp : App(LiveDashboard::class, Styles::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<LiveDashboardApp>(args)
        }
    }
}

class LiveDashboard : View("Live Dashboard") {
    override val root = vbox {
        label("Starting Position On Field").paddingAll = 20
        val startingPositions = FXCollections.observableArrayList("Left", "Center", "Right")
        combobox<String> {
            items = startingPositions
            setOnAction { println("Clicked") }
        }

        val autoModes = FXCollections.observableArrayList("3 Cube, 2 Cube")
        combobox<String> {
            items = autoModes
        }
    }

}

class Styles : Stylesheet() {
    init {
        label {
            fontFamily = "Kanit Bold"
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            backgroundColor += c("#cecece")
        }
        comboBox {
            fontFamily = "Kanit Bold"
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            backgroundColor += c("#cecece")
        }
    }
}
