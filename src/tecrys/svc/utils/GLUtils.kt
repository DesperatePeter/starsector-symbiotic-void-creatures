package tecrys.svc.utils

import com.fs.starfarer.api.Global
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import java.awt.Color

val screenWidth = Global.getSettings().screenWidth
val screenHeight = Global.getSettings().screenHeight

fun preRender(){
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glPushMatrix()
    GL11.glLoadIdentity()
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glPushMatrix()
    GL11.glLoadIdentity()
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glViewport(0,0, Display.getWidth(), Display.getHeight())
    GL11.glOrtho(0.0, Display.getWidth().toDouble(),0.0, Display.getHeight().toDouble(),-1.0, 1.0)
    GL11.glTranslatef(0.01f, 0.01f, 0f)
    GL11.glEnable(GL11.GL_LINE_SMOOTH)
    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
    GL11.glLineWidth(1.5f)
}

fun postRender(){
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glPopMatrix()
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glPopMatrix()
    GL11.glPopAttrib()
}

fun setColor(color: Color){
    GL11.glColor4f(color.red.toFloat()/255f, color.green.toFloat()/255f, color.blue.toFloat()/255f, color.alpha.toFloat()/255f)
}