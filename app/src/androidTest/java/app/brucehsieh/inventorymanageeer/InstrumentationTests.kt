package app.brucehsieh.inventorymanageeer

import android.view.View
import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.brucehsieh.inventorymanageeer.TestUtil.clickChildViewWithId
import app.brucehsieh.inventorymanageeer.TestUtil.findAndCheckTheView
import app.brucehsieh.inventorymanageeer.TestUtil.itemAtPosition
import app.brucehsieh.inventorymanageeer.TestUtil.recyclerViewItemCounts
import app.brucehsieh.inventorymanageeer.TestUtil.swipeDownOnSwipeRefreshLayout
import app.brucehsieh.inventorymanageeer.storefront.presentation.InventoryAdapter
import com.google.android.material.slider.Slider
import org.hamcrest.CoreMatchers.*
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

//    private val walmartKey = System.getenv("WalmartKey")
//    private val walmartSecret = System.getenv("WalmartSecret")

    @Test
    fun recyclerView_stayAtTop_listingLoadedCorrectly() {
        // Arrange
        val firstProductSku = "G1-35ZF-RTBU"
        val resourceId = R.id.text_product_sku

        // Act
        // Assert
        findAndCheckTheView(
            viewInteraction = onView(allOf(withId(resourceId), withText(firstProductSku))),
            viewAssertion = matches(isDisplayed())
        )
    }

    @Test
    fun recyclerView_scrollToBottom_listingsLoadedCorrectly() {
        // Arrange
        val resourceId = R.id.text_product_sku
        val firstProductSku = "G1-35ZF-RTBU"
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
            viewAssertion = matches(itemAtPosition(0, not(hasDescendant(withText("-1")))))
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
            viewAssertion = matches(itemAtPosition(0,
                hasDescendant(withText(newQuantity.toString()))))
        )
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
            viewAssertion = matches(itemAtPosition(0, not(hasDescendant(withText("-1")))))
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
            viewAssertion = matches(itemAtPosition(0,
                hasDescendant(withText(newQuantity.toString()))))
        )
    }

    @Test
    fun swipeRefreshLayout_swipeDown_emptyListingsAndThenLoadListingsSuccessfully() {
        // - Arrange
        val itemCountAfterSwipeDown = 0
        val firstProductSku = "G1-35ZF-RTBU"

        // - Act
        onView(withId(R.id.swipe_refresh_layout))
            .perform(swipeDownOnSwipeRefreshLayout(swipeDown()))

        // - Assert
        // RecyclerView is empty right after swipe down the SwipeRefreshLayout
        onView(withId(R.id.listing_recycler_view))
            .check(matches(recyclerViewItemCounts(itemCountAfterSwipeDown)))

        // RecyclerView will be populated later
        findAndCheckTheView(
            viewInteraction = onView(
                allOf(withId(R.id.text_product_sku), withText(firstProductSku))
            ),
            viewAssertion = matches(isDisplayed())
        )
    }

    @Test
    fun swipeRefreshLayout_goToShopifyPageAndSwipeDown_emptyListingsAndThenLoadListingsSuccessfully() {
        // - Arrange
        val itemCountAfterSwipeDown = 0
        val secondProductSku = "DF-YYLJ-IISZ"

        // - Act
        // Click Shopify tab
        onView(withId(R.id.tab_layout))
            .perform(TestUtil.selectTabAt(NavigationUITest.SHOPIFY_TAB_POSITION))

        // Swipe down
        onView(withId(R.id.swipe_refresh_layout))
            .perform(swipeDownOnSwipeRefreshLayout(swipeDown()))

        // - Assert
        // RecyclerView is empty right after swipe down the SwipeRefreshLayout
        onView(withId(R.id.listing_recycler_view))
            .check(matches(recyclerViewItemCounts(itemCountAfterSwipeDown)))

        // RecyclerView will be populated later
        findAndCheckTheView(
            viewInteraction = onView(
                allOf(withId(R.id.text_product_sku), withText(secondProductSku))
            ),
            viewAssertion = matches(isDisplayed())
        )
    }

//    @Test
//    fun marketKeyDialog_enterKeyAndSecretForWalmart_stayAtTop_listingLoadedCorrectly() {
//        // Arrange
//        val resourceId = R.id.text_product_sku
//        val productSku = "2Y-0GQX-Z9Q0"
//
//        // Act
//        typeKeyAndSecret(key = walmartKey, secret = walmartSecret)
//
//        onView(withId(R.id.update_key_value_button))
//            .inRoot(isDialog())
//            .check(matches(isDisplayed()))
//            .perform(click())
//
//        // Assert
//        findAndCheckTheView(
//            viewInteraction = onView(allOf(withId(resourceId), withText(productSku))),
//            viewAssertion = matches(isDisplayed())
//        )
//    }

    /**
     * Enter [key] and [secret] for a given marketplace.
     * */
    private fun typeKeyAndSecret(key: String, secret: String) {
        onView(withId(R.id.key_text_input))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(typeText(key))

        onView(withId(R.id.secret_text_input))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(typeText(secret))
            /**
             * Must close Soft Keyboard so we click the update button.
             * */
            .perform(closeSoftKeyboard())
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