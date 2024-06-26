// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ion

/**
 * **NOT FOR APPLICATION USE. This method may be removed at any time.**
 * Trampoline to the non-public `Timestamp.toString(Int)` method.
 */
internal fun printTimestamp(timestamp: Timestamp, maximumDigits: Int): String {
    return timestamp.toString(maximumDigits)
}
