package world.hachimi.app.ui.userspace.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.user_space_female_cd
import hachimiworld.composeapp.generated.resources.user_space_male_cd
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun GenderIcon(
    gender: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(16.dp)) {
        when (gender) {
            0 -> Icon(
                Icons.Default.Male,
                contentDescription = stringResource(Res.string.user_space_male_cd)
            )

            1 -> Icon(
                Icons.Default.Female,
                contentDescription = stringResource(Res.string.user_space_female_cd)
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        GenderIcon(gender = 0)
    }
}
