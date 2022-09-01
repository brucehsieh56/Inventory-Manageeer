package app.brucehsieh.inventorymanageeer

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.brucehsieh.inventorymanageeer.TestUtil.clickChildViewWithId
import app.brucehsieh.inventorymanageeer.TestUtil.itemAtPosition
import app.brucehsieh.inventorymanageeer.TestUtil.findAndCheckTheView
import app.brucehsieh.inventorymanageeer.storefront.presentation.InventoryAdapter
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationUITest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    companion object {
        private const val WALMART_TITLE = "Walmart"
        private const val SHOPIFY_TITLE = "Shopify"

        private const val WALMART_TAB_POSITION = 0
        private const val SHOPIFY_TAB_POSITION = 1
    }

    @Test
    fun goToShopifyPage_success() {
        // Check the Shopify tab is not selected in the beginning
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(SHOPIFY_TITLE)
            )
        ).check(
            ViewAssertions.matches(not(ViewMatchers.isSelected()))
        )

        // Check the Walmart tab is selected in the beginning
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(WALMART_TITLE)
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isSelected())
        )

        // Click Shopify tab
        Espresso.onView(
            ViewMatchers.withId(R.id.tab_layout)
        ).perform(
            TestUtil.selectTabAt(SHOPIFY_TAB_POSITION)
        )

        // Check if the Shopify tab is selected
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(SHOPIFY_TITLE)
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isSelected())
        )
    }

    @Test
    fun goToShopifyPageAndBackToWalmartPage_success() {
        // Click Shopify tab
        Espresso.onView(
            ViewMatchers.withId(R.id.tab_layout)
        ).perform(
            TestUtil.selectTabAt(SHOPIFY_TAB_POSITION)
        )

        // Click Walmart tab
        Espresso.onView(
            ViewMatchers.withId(R.id.tab_layout)
        ).perform(
            TestUtil.selectTabAt(WALMART_TAB_POSITION)
        )

        // Check if the Shopify tab is not selected
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(SHOPIFY_TITLE)
            )
        ).check(
            ViewAssertions.matches(not(ViewMatchers.isSelected()))
        )

        // Check if the Walmart tab is selected
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(WALMART_TITLE)
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isSelected())
        )
    }

    @Test
    fun goToShopifyPageAndGoToShopifyPageAgain_nothingHappened_success() {
        // Click Shopify tab
        Espresso.onView(
            ViewMatchers.withId(R.id.tab_layout)
        ).perform(
            TestUtil.selectTabAt(SHOPIFY_TAB_POSITION)
        )

        // Click Shopify tab again
        Espresso.onView(
            ViewMatchers.withId(R.id.tab_layout)
        ).perform(
            TestUtil.selectTabAt(SHOPIFY_TAB_POSITION)
        )

        // Check if the Walmart tab not is selected
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(WALMART_TITLE)
            )
        ).check(
            ViewAssertions.matches(not(ViewMatchers.isSelected()))
        )

        // Check if the Shopify tab is selected
        Espresso.onView(
            CoreMatchers.allOf(
                CoreMatchers.instanceOf(TabLayout.TabView::class.java),
                ViewMatchers.withContentDescription(SHOPIFY_TITLE)
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isSelected())
        )
    }

    @Test
    fun clickInventoryField_inventoryAdjustDialogLaunch_success() {
        // Arrange
        val dummyPosition = 0
        val initialDummyQuantity = "-1"

        /**
         * Wait for inventory to get update.
         * */
        findAndCheckTheView(
            viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.listing_recycler_view)),
            viewAssertion = ViewAssertions.matches(
                itemAtPosition(
                    dummyPosition,
                    not(ViewMatchers.hasDescendant(ViewMatchers.withText(initialDummyQuantity)))
                )
            )
        )

        // Act
        Espresso.onView(ViewMatchers.withId(R.id.listing_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<InventoryAdapter.ViewHolder>(
                    dummyPosition,
                    clickChildViewWithId(R.id.product_inventory)
                )
            )

        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.inventory_dialog_title))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}