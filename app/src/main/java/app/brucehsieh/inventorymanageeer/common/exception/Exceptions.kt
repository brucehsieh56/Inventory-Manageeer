package app.brucehsieh.inventorymanageeer.common.exception

import java.io.IOException

class NetworkUnavailableException(message: String = "No network available") : IOException(message)