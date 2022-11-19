package com.andb.apps.weather.ui.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import java.util.concurrent.CancellationException

suspend fun PointerInputScope.detectDragGesturesAfterLongPressSilent(
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    awaitEachGesture {
        try {
            val down = awaitFirstDown(requireUnconsumed = false)
            val drag = awaitLongPressOrCancellation(down.id)
            if (drag != null) {
                onDragStart.invoke(drag.position)

                if (
                    drag(drag.id) {
                        onDrag(it, it.positionChange())
                        //it.consume()
                    }
                ) {
//                    // consume up if we quit drag gracefully with the up
//                    currentEvent.changes.forEach {
//                        if (it.changedToUp()) it.consume()
//                    }
                    onDragEnd()
                } else {
                    onDragCancel()
                }
            }
        } catch (c: CancellationException) {
            onDragCancel()
            throw c
        }
    }
}