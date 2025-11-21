package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize

@Composable
fun Pagination(
    modifier: Modifier = Modifier,
    total: Int,
    currentPage: Int,
    pageSize: Int = 10,
    pageSizes: List<Int> = remember { listOf(10, 20, 30) },
    pageSizeChange: (Int) -> Unit,
    pageChange: (Int) -> Unit
) {
    val pageCount = remember(total, pageSize) {
        if (total == 0) return@remember 1
        var count = total / pageSize
        if (total % pageSize != 0) {
            count++
        }
        return@remember count
    }

    BoxWithConstraints(
        modifier = modifier,
        propagateMinConstraints = true
    ) {
        if (maxWidth < WindowSize.COMPACT) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreviousButton(currentPage, pageChange)
                    PaginationComponent(
                        modifier = Modifier.weight(1f),
                        pageCount = pageCount,
                        currentPage = currentPage,
                        onPageChange = pageChange
                    )
                    NextButton(currentPage, pageCount, pageChange)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("共 $total 条", style = MaterialTheme.typography.labelMedium)

                    PageSizeButton(
                        pageSize = pageSize,
                        pageSizes = pageSizes,
                        pageSizeChange = pageSizeChange
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreviousButton(currentPage, pageChange)
                PaginationComponent(
                    modifier = Modifier.weight(1f),
                    pageCount = pageCount,
                    currentPage = currentPage,
                    onPageChange = pageChange
                )
                NextButton(currentPage, pageCount, pageChange)

                Text("共 $total 条", style = MaterialTheme.typography.labelMedium)

                PageSizeButton(
                    pageSize = pageSize,
                    pageSizes = pageSizes,
                    pageSizeChange = pageSizeChange
                )
            }
        }
    }
}


@Composable
private fun PreviousButton(
    currentPage: Int,
    onPageChange: (page: Int) -> Unit
) {
    IconButton(onClick = {
        if (currentPage > 0) {
            onPageChange(currentPage - 1)
        }
    }) {
        Icon(Icons.Default.ChevronLeft, "Previous")
    }
}

@Composable
private fun NextButton(
    currentPage: Int,
    pageCount: Int,
    onPageChange: (page: Int) -> Unit
) {
    IconButton(onClick = {
        if (currentPage < pageCount - 1) {
            onPageChange(currentPage + 1)
        }
    }) {
        Icon(Icons.Default.ChevronRight, "Next")
    }
}

@Composable
private fun PaginationComponent(
    modifier: Modifier,
    pageCount: Int,
    currentPage: Int,
    onPageChange: (page: Int) -> Unit
) {
    val state = rememberLazyListState()
    val offset = with(LocalDensity.current) { 48.dp.roundToPx() }
    LaunchedEffect(currentPage) {
        state.animateScrollToItem(currentPage, -offset)
    }
    LazyRow(modifier, state) {
        items(pageCount) { i ->
            val checked = currentPage == i
            IconToggleButton(
                checked = checked,
                onCheckedChange = {
                    if (it) onPageChange(i)
                }
            ) {
                Text((i + 1).toString())
            }
        }
    }
}

@Composable
private fun PageSizeButton(
    pageSize: Int = 10,
    pageSizes: List<Int> = remember { listOf(10, 20, 30, 50, 100) },
    pageSizeChange: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text("$pageSize 条/页")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (x in pageSizes) {
                DropdownMenuItem(onClick = {
                    pageSizeChange(x)
                    expanded = false
                }, text = {
                    Text(x.toString())
                })
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        Pagination(
            total = 100,
            currentPage = 0,
            pageSize = 10,
            pageSizes = listOf(10, 20, 30, 50, 100),
            pageSizeChange = {},
            pageChange = {}
        )
    }
}