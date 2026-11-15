package dev.adrian.showdown

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Checks that the Showdown client WebView exists and loads; what it renders is Showdown's own code. */
@RunWith(AndroidJUnit4::class)
class ShowdownBattleSceneViewTest {
    private val pageLoadTimeoutMillis = TimeUnit.SECONDS.toMillis(30)
    private val tooltipTimeoutMillis = TimeUnit.SECONDS.toMillis(60)
    private val battleWithAFoe = listOf(
        "|init|battle",
        "|player|p1|PLAYER||",
        "|player|p2|OPPONENT||",
        "|gametype|singles",
        "|gen|9",
        "|start",
        "|switch|p1a: Eevee|Eevee, L80|100/100",
        "|switch|p2a: Mew|Mew, L80|100/100",
        "|turn|1"
    )
    private val minimalBattle = listOf(
        "|init|battle",
        "|player|p1|PLAYER||",
        "|player|p2|OPPONENT||",
        "|gametype|singles",
        "|start"
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(ShowdownLogParityHarnessActivity::class.java)

    @Test
    fun ShowdownMoveEffectsView_CreatedByTheHostActivity_IsAttachedToItsWindow() {
        lateinit var activity: ShowdownLogParityHarnessActivity
        activityRule.scenario.onActivity { activity = it }

        val parent = activity.renderer.parent

        assertNotNull("The battle scene view is not attached to a window", parent)
    }

    @Test
    fun ShowdownMoveEffectsView_GivenAMinimalBattle_LoadsItsPageAndReportsTheBattleLog() {
        lateinit var activity: ShowdownLogParityHarnessActivity
        activityRule.scenario.onActivity { activity = it }
        val session = BattleSession().apply { setLocalUsername("PLAYER") }
        session.applyProtocolPacket(minimalBattle)
        val generation = session.battleLogGeneration()
        val synchronizedBefore = activity.synchronizedGenerations.size

        activity.renderer.applyProtocol(minimalBattle, generation)
        val synchronized = activity.awaitBattleLogSynchronization(synchronizedBefore, generation, pageLoadTimeoutMillis)

        assertTrue("The battle scene page did not load and sync within $pageLoadTimeoutMillis ms", synchronized)
        assertTrue("The page raised JavaScript errors: ${activity.javascriptErrors}", activity.javascriptErrors.isEmpty())
    }

    @Test
    fun ShowdownBattleSceneView_TapOnTheFoe_ShowsItsDetails() {
        lateinit var activity: ShowdownLogParityHarnessActivity
        activityRule.scenario.onActivity { activity = it }
        showBattleWithAFoe(activity)

        val tapPoint = activity.foeHitBoxCentre()
        tapView(activity, tapPoint.first, tapPoint.second)
        val tooltip = activity.waitForTooltipContaining("Mew", tooltipTimeoutMillis)

        assertTrue("No tooltip with the foe's details appeared after tapping it; last text: '$tooltip'", tooltip.contains("Mew"))
    }

    private fun showBattleWithAFoe(activity: ShowdownLogParityHarnessActivity) {
        val session = BattleSession().apply { setLocalUsername("PLAYER") }
        session.applyProtocolPacket(battleWithAFoe)
        val generation = session.battleLogGeneration()
        val synchronizedBefore = activity.synchronizedGenerations.size
        activity.renderer.applyProtocol(battleWithAFoe, generation)
        activity.awaitBattleLogSynchronization(synchronizedBefore, generation, pageLoadTimeoutMillis)
    }

    private fun tapView(activity: ShowdownLogParityHarnessActivity, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        activity.runOnUiThread {
            val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
            val up = MotionEvent.obtain(downTime, downTime + TAP_DURATION_MILLIS, MotionEvent.ACTION_UP, x, y, 0)
            activity.renderer.dispatchTouchEvent(down)
            activity.renderer.dispatchTouchEvent(up)
            down.recycle()
            up.recycle()
        }
    }

    private fun ShowdownLogParityHarnessActivity.foeHitBoxCentre(): Pair<Float, Float> {
        val deadline = SystemClock.uptimeMillis() + pageLoadTimeoutMillis
        while (SystemClock.uptimeMillis() < deadline) {
            val centre = evaluate(FOE_HIT_BOX_CENTRE_SCRIPT).trim('"')
            val parts = centre.split(',')
            if (parts.size == 2) {
                val density = resources.displayMetrics.density
                return parts[0].toFloat() * density to parts[1].toFloat() * density
            }
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        throw AssertionError("The foe's tap target never appeared on the page")
    }

    private fun ShowdownLogParityHarnessActivity.waitForTooltipContaining(text: String, timeoutMillis: Long): String {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        var last = ""
        while (SystemClock.uptimeMillis() < deadline) {
            last = evaluate(TOOLTIP_TEXT_SCRIPT)
            if (last.contains(text)) return last
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        return last
    }

    private fun ShowdownLogParityHarnessActivity.evaluate(script: String): String {
        val result = AtomicReference("")
        val latch = CountDownLatch(1)
        runOnUiThread {
            renderer.evaluateJavascript(script) {
                result.set(it.orEmpty())
                latch.countDown()
            }
        }
        latch.await(pageLoadTimeoutMillis, TimeUnit.MILLISECONDS)
        return result.get()
    }

    private companion object {
        const val TAP_DURATION_MILLIS = 60L
        const val POLL_INTERVAL_MILLIS = 500L
        const val FOE_HIT_BOX_CENTRE_SCRIPT = "(function(){var e=document.querySelector(\".has-tooltip[data-tooltip='activepokemon|1|0']\");" +
            "if(!e)return '';var r=e.getBoundingClientRect();return (r.left+r.width/2)+','+(r.top+r.height/2);})()"
        const val TOOLTIP_TEXT_SCRIPT = "(function(){var w=document.getElementById('tooltipwrapper');return w?w.innerText:'';})()"
    }
}
