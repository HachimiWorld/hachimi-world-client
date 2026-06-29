package world.hachimi.app.ui.follow.components

import androidx.compose.runtime.Composable
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

@Composable
fun UnfollowDialog(
    username: String,
    subtitle: String,
    confirmText: String,
    cancelText: String,
    confirmTitle: String,
    loading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(confirmTitle) },
        text = { Text(subtitle) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !loading) {
                Text(if (loading) "..." else confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        },
    )
}
