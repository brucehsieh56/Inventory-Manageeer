package app.brucehsieh.inventorymanageeer

import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.brucehsieh.inventorymanageeer.ui.inventory.InventoryAdapter
import com.google.android.material.slider.Slider
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class InstrumentationTests {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private val timeoutMillis: Int = 1_000
    private val attemptTimeoutMillis: Long = 100L

    @Test
    fun recyclerView_stayAtTop_listingsLoadedCorrectly() {
        // Arrange
        val resourceId = R.id.product_sku
        val productSku = "2Y-0GQX-Z9Q0"

        // Act
        // Assert
        findAndCheckTheView(
            viewInteraction = onView(allOf(withId(resourceId), withText(productSku))),
            viewAssertion = matches(isDisplayed())
        )
    }

    @Test
    fun recyclerView_scrollToBottom_listingsLoadedCorrectly() {
        // Arrange
        val resourceId = R.id.product_sku
        val firstProductSku = "2Y-0GQX-Z9Q0"
        val lastProductSku = "OG015"

        val recyclerViewId = R.id.listing_recycler_view
        val itemPosition = 19

        // Act
        findAndCheckTheView(
            viewInteraction = onView(allOf(withId(resourceId), withText(firstProductSku))),
            viewAssertion = matches(isDisplayed())
        )

        onView(withId(recyclerViewId))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<InventoryAdapter.ViewHolder>(
                    itemPosition,
                    click()
                )
            )

        // Assert
        onView(allOf(withId(resourceId), withText(lastProductSku)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun recyclerView_updateInventoryBySlider_clickInventoryEditTextToLaunchInventoryDialog_correct() {
        // Arrange
        val newQuantity = List(10) { Random.nextInt(50, 100) }.shuffled().first()
        println("newQuantity is $newQuantity")

        // Act
        // Assert

        /**
         * Wait for inventory to get update.
         * */
        findAndCheckTheView(
            viewInteraction = onView(withId(R.id.listing_recycler_view)),
            viewAssertion = matches(atPosition(0, not(hasDescendant(withText("-1")))))
        )

        /**
         * Once inventory is updated, click the inventory [EditText] of the first item to launch the
         * [InventoryAdjustDialog].
         * */
        onView(withId(R.id.listing_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<InventoryAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.product_inventory)
                )
            )

        /**
         * Click the [EditText] on [InventoryAdjustDialog] and adjust by [newQuantity].
         *
         * Move [Slider] to the value [newQuantity].
         * */
        onView(withId(R.id.quantity_slider))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(setSliderValue(newQuantity))

        /**
         * Click UPDATE button to update the inventory.
         * */
        onView(withText("Update"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        /**
         * Ensure the inventory has the correct [newQuantity].
         * */
        findAndCheckTheView(
            viewInteraction = onView(withId(R.id.listing_recycler_view)),
            viewAssertion = matches(atPosition(0, hasDescendant(withText(newQuantity.toString()))))
        )
    }

    @Test
    fun recyclerView_updateInventoryByEditText_clickInventoryEditTextToLaunchInventoryDialog_correct() {
        // Arrange
        val newQuantity = List(10) { Random.nextInt(50, 100) }.shuffled().first()
        println("newQuantity is $newQuantity")

        // Act
        // Assert

        /**
         * Wait for inventory to get update.
         * */
        findAndCheckTheView(
            viewInteraction = onView(withId(R.id.listing_recycler_view)),
            viewAssertion = matches(atPosition(0, not(hasDescendant(withText("-1")))))
        )

        /**
         * Once inventory is updated, click the inventory [EditText] of the first item to launch the
         * [InventoryAdjustDialog].
         * */
        onView(withId(R.id.listing_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<InventoryAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.product_inventory)
                )
            )

        /**
         * Click the [EditText] on [InventoryAdjustDialog] and adjust by [newQuantity].
         *
         * [clearText] first, then [replaceText] with [newQuantity].
         * */
        onView(withId(R.id.product_quantity))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click(), clearText())

        Thread.sleep(attemptTimeoutMillis)
        onView(withId(R.id.product_quantity))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click(), replaceText(newQuantity.toString()))

        /**
         * Click UPDATE button to update the inventory.
         * */
        onView(withText("Update"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        /**
         * Ensure the inventory has the correct [newQuantity].
         * */
        findAndCheckTheView(
            viewInteraction = onView(withId(R.id.listing_recycler_view)),
            viewAssertion = matches(atPosition(0, hasDescendant(withText(newQuantity.toString()))))
        )
    }

    /**
     * Find and check the view.
     * */
    private fun findAndCheckTheView(
        viewInteraction: ViewInteraction,
        viewAssertion: ViewAssertion = matches(isDisplayed()),
        timeoutMillis: Int = 1_0000,
        attemptTimeoutMillis: Long = 100L
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

    /**
     * Check the child view.
     * */
    private fun clickChildViewWithId(id: Int): ViewAction {
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
     * Check the item of a [RecyclerView].
     * */
    private fun atPosition(position: Int, itemMatchers: Matcher<View?>): Matcher<View?> {
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
     * Set new [sliderValue] to a [Slider].
     * */
    private fun setSliderValue(sliderValue: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(Slider::class.java)

            override fun getDescription(): String = "Set slider value"

            override fun perform(uiController: UiController?, view: View?) {
                val slider = view as Slider
                slider.value = sliderValue.toFloat()
            }
        }
    }

    /**
     * Set the cursor position for a [EditText].
     * */
    private fun setCursorPositionForEditText(cursorPosition: Int = 1): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(EditText::class.java)

            override fun getDescription(): String = "Set cursor position to $cursorPosition"

            override fun perform(uiController: UiController?, view: View?) {
                val editText = view as EditText
                editText.setSelection(0, cursorPosition)
            }
        }
    }
}