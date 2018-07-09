package frc.team5190.lib.geometry.interfaces

interface ICurvature<S> : State<S> {
    val curvature: Double
    val dkds: Double
}
