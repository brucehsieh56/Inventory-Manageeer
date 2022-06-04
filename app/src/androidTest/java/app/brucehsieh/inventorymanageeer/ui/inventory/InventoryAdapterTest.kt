package app.brucehsieh.inventorymanageeer.ui.inventory

import app.brucehsieh.inventorymanageeer.model.WalmartListing
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InventoryAdapterTest {

    private lateinit var inventoryAdapter: InventoryAdapter

    @Before
    fun setUp() {
        inventoryAdapter = InventoryAdapter()
    }

    @Test
    fun inventoryAdapter_submitData_hasCorrectData() {
        // Arrange
        val data1 = WalmartListing(
            productName = "productName1",
            productSku = "productSku1",
            quantity = 10,
            price = 9.99F
        )

        val data2 = WalmartListing(
            productName = "productName2",
            productSku = "productSku2",
            quantity = 11,
            price = 8.99F
        )

        val dataset = listOf(data1, data2)

        // Act
        inventoryAdapter.submitList(dataset)

        // Assert
        assertEquals(inventoryAdapter.itemCount, dataset.size)
        assertEquals(inventoryAdapter.currentList.first(), data1)
    }
}