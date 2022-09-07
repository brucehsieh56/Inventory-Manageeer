package app.brucehsieh.inventorymanageeer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher

object TestUtil {

    /**
     * Find and check the view given a time constraint [timeoutMillis].
     * */
    fun findAndCheckTheView(
        viewInteraction: ViewInteraction,
        viewAssertion: ViewAssertion = ViewAssertions.matches(isDisplayed()),
        timeoutMillis: Int = 1_0000,
        attemptTimeoutMillis: Long = 100L,
    ) {
        val maxAttempts = timeoutMillis / attemptTimeoutMillis.toInt()
        var attempts = 0
        for (i in 0..maxAttempts) {
            try {
                attempts++
                viewInteraction.check(viewAssertion)
            } catch (t: Throwable) {
                if (attempts == maxAttempts) {
                    throw t
                }
                Thread.sleep(attemptTimeoutMillis)
            }
        }
    }

    // ViewAction ==================================================================================

    /**
     * Select a [TabLayout.Tab] from [TabLayout] given a [tabPosition].
     * */
    fun selectTabAt(tabPosition: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))
            }

            override fun getDescription(): String = "Get tab at position $tabPosition"

            override fun perform(uiController: UiController?, view: View?) {
                val tabLayout = view as TabLayout
                val tab = tabLayout.getTabAt(tabPosition)
                    ?: throw IllegalArgumentException("Tab not exist at position $tabPosition")
                tab.select()
            }
        }
    }

    /**
     * Click the child view.
     * */
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? = null

            override fun getDescription(): String = "Click on a child view given a resource id."

            override fun perform(uiController: UiController?, view: View?) {
                val v = view?.findViewById<View>(id)
                v?.performClick()
            }
        }
    }

    /**
     * Reference: https://stackoverflow.com/a/33516360
     * */
    fun swipeDownOnSwipeRefreshLayout(viewAction: ViewAction): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(85)

            override fun getDescription(): String = "Swipe down on SwipeRefreshLayout"

            override fun perform(uiController: UiController?, view: View?) {
                viewAction.perform(uiController, view)
            }
        }
    }

    // Matcher =====================================================================================

    /**
     * Check the item of a [RecyclerView].
     * */
    fun itemAtPosition(position: Int, itemMatchers: Matcher<View?>): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description?) {
                description?.appendText("has item at position $position")
            }

            override fun matchesSafely(item: RecyclerView?): Boolean {
                val viewHolder = item?.findViewHolderForAdapterPosition(position) ?: return false
                return itemMatchers.matches(viewHolder.itemView)
            }
        }
    }

    /**
     * Check the number of items for a [RecyclerView].
     *
     * Reference: https://stackoverflow.com/a/63583748
     * */
    fun recyclerViewItemCounts(numberOfItems: Int): BoundedMatcher<View?, RecyclerView> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description?) {
                description?.appendText("with number of items: $numberOfItems")
            }

            override fun matchesSafely(item: RecyclerView?): Boolean {
                return numberOfItems == item!!.adapter!!.itemCount
            }
        }
    }
}