package tecrys.svc.utils

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

class DelayedMusicPlayer(
    private val musicId: String?,
    private val fadeIn: Int,
    private val fadeOut: Int,
    private val shouldLoop: Boolean
): EveryFrameScript {

    companion object{
        fun playDelayedMusic(musicId: String?, fadeIn: Int, fadeOut: Int, shouldLoop: Boolean){
            Global.getSector().addTransientScript(
                DelayedMusicPlayer(musicId, fadeIn, fadeOut, shouldLoop)
            )
        }
    }

    private var counter = 0
    private var isDone = false
    override fun isDone(): Boolean = isDone || musicId == null

    override fun runWhilePaused(): Boolean = true

    override fun advance(p0: Float) {
        if(counter++ >= 2){
            isDone = true
            musicId?.let { m ->
                Global.getSoundPlayer().playCustomMusic(1, 1, m, shouldLoop)
            }
        }
    }
}