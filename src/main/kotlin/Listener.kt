import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import org.jnativehook.NativeInputEvent.*
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyEvent.VC_BACK_SLASH
import org.jnativehook.keyboard.NativeKeyEvent.VC_ESCAPE
import org.jnativehook.keyboard.NativeKeyListener
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class Listener(val takeAction: (String) -> Unit?) : NativeKeyListener {
    var ctrlDown = AtomicBoolean(false)
    var activated = AtomicBoolean(false)
    var deactivated = AtomicBoolean(false)
    @Volatile var active = AtomicBoolean(false)

    @Volatile var query = ""

    init {
        try {
            Logger.getLogger(GlobalScreen::class.java.getPackage().name).apply {
                level = Level.WARNING
                useParentHandlers = false
            }

            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            System.err.println(ex.message)
            exitProcess(1)
        }

        GlobalScreen.addNativeKeyListener(this)
    }

    val ctrlKeys = listOf(CTRL_MASK, CTRL_L_MASK, CTRL_R_MASK, 29)

    override fun nativeKeyPressed(keyEvent: NativeKeyEvent) {
        if (keyEvent.keyCode in ctrlKeys) {
            ctrlDown.set(true)
        } else if (keyEvent.keyCode == VC_BACK_SLASH && ctrlDown.compareAndSet(true, false)) {
            activated.set(true)
            active.set(true)
        } else if (keyEvent.keyCode == VC_ESCAPE) {
            deactivated.set(true)
        }
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent) {
        if (p0.keyCode in ctrlKeys) ctrlDown.set(false)
    }

    override fun nativeKeyTyped(keyEvent: NativeKeyEvent) {
        if (keyEvent.keyChar.isLetterOrDigit() && active.get()) {
            query += keyEvent.keyChar.toString()
            takeAction(query.takeLast(2))
        }
    }
}