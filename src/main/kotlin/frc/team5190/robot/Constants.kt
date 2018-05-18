package frc.team5190.robot

object MotorIDs {
    const val FRONT_LEFT = 1
    const val REAR_LEFT = 2
    const val FRONT_RIGHT = 3
    const val REAR_RIGHT = 4

    const val ELEVATOR_MASTER = 5
    const val ELEVATOR_SLAVE = 6
    const val ARM = 8

    const val INTAKE_MASTER = 7
    const val INTAKE_SLAVE = 9

    const val WINCH_MASTER = 10
    const val WINCH_SLAVE = 59
}

object ChannelIDs {
    const val LEFT_CUBE_SENSOR = 2
    const val RIGHT_CUBE_SENSOR = 3

    const val LIDAR_SERVO = 0
}

object SolenoidIDs {
    const val PCM = 41

    const val DRIVE = 3
    const val INTAKE = 2
}

object DriveConstants {
    const val SENSOR_UNITS_PER_ROTATION = 1440
    const val WHEEL_RADIUS = 3.0
}