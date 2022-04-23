package me.ash.reader.ui.page.settings

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.CurlyCornerShape
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.theme.palette.alwaysLight
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsAndSupport(
    navController: NavHostController,
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val viewState = updateViewModel.viewState.collectAsStateValue()
    val githubLink = stringResource(R.string.github_link)
    val telegramLink = stringResource(R.string.telegram_link)
    val isLatestVersion = stringResource(R.string.is_latest_version)
    var currentVersion by remember { mutableStateOf("") }
    var pressAMP by remember { mutableStateOf(16f) }
    val animatedPress by animateFloatAsState(
        targetValue = pressAMP,
        animationSpec = tween()
    )

    LaunchedEffect(Unit) {
        currentVersion = context.getCurrentVersion().toString()
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface
                ),
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        navController.popBackStack()
                    }
                },
                actions = {}
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                item {
                    Column(
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    pressAMP = 0f
                                    tryAwaitRelease()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    pressAMP = 16f
                                },
                                onTap = {
                                    scope.launch {
                                        context.dataStore.put(DataStoreKeys.SkipVersionNumber, "")
                                        updateViewModel.dispatch(
                                            UpdateViewAction.CheckUpdate(
                                                {
                                                    context.dataStore.put(
                                                        DataStoreKeys.SkipVersionNumber,
                                                        ""
                                                    )
                                                },
                                                {
                                                    if (!it) Toast.makeText(
                                                        context,
                                                        isLatestVersion,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        )
                                    }
                                }
                            )
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                                    shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                                )
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                                    ambientColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                                    spotColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                modifier = Modifier.size(90.dp),
                                painter = painterResource(R.drawable.ic_launcher_monochrome),
                                contentDescription = stringResource(R.string.read_you),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface alwaysLight true),
                            )
                        }
                        Spacer(modifier = Modifier.height(48.dp))
                        BadgedBox(
                            badge = {
                                Badge(
                                    modifier = Modifier.animateContentSize(tween(800)),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                ) {
                                    Text(text = currentVersion)
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.read_you),
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Sponsor
                        RoundIconButton(RoundIconButtonType.Sponsor(
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer alwaysLight true,
                        ) {})
                        Spacer(modifier = Modifier.width(16.dp))

                        // Telegram
                        RoundIconButton(RoundIconButtonType.Telegram(
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(telegramLink)
                                )
                            )
                        })
                        Spacer(modifier = Modifier.width(16.dp))

                        // GitHub
                        RoundIconButton(RoundIconButtonType.GitHub(
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(githubLink)
                                )
                            )
                        })
                        Spacer(modifier = Modifier.width(16.dp))

                        // License
                        RoundIconButton(RoundIconButtonType.License(
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer alwaysLight true,
                        ) {})
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    )

    UpdateDialog(
        visible = viewState.updateDialogVisible,
        onDismissRequest = { updateViewModel.dispatch(UpdateViewAction.Hide) },
    )
}

@Immutable
sealed class RoundIconButtonType(
    val iconResource: Int? = null,
    val iconVector: ImageVector? = null,
    val descResource: Int? = null,
    val descString: String? = null,
    open val offset: Modifier = Modifier.offset(),
    open val backgroundColor: Color = Color.Unspecified,
    open val onClick: () -> Unit = {},
) {
    @Immutable
    data class Sponsor(
        val desc: Int = R.string.sponsor,
        override val backgroundColor: Color,
        override val onClick: () -> Unit = {},
    ) : RoundIconButtonType(
        iconVector = Icons.Rounded.VolunteerActivism,
        descResource = desc,
        backgroundColor = backgroundColor,
        onClick = onClick,
    )

    @Immutable
    data class Telegram(
        val desc: String = "Telegram",
        override val offset: Modifier = Modifier.offset(x = (-1).dp),
        override val backgroundColor: Color,
        override val onClick: () -> Unit = {},
    ) : RoundIconButtonType(
        iconResource = R.drawable.ic_telegram,
        descString = desc,
        backgroundColor = backgroundColor,
        onClick = onClick,
    )

    @Immutable
    data class GitHub(
        val desc: String = "GitHub",
        override val backgroundColor: Color,
        override val onClick: () -> Unit = {},
    ) : RoundIconButtonType(
        iconResource = R.drawable.ic_github,
        descString = desc,
        backgroundColor = backgroundColor,
        onClick = onClick,
    )

    @Immutable
    data class License(
        val desc: Int = R.string.open_source_licenses,
        override val backgroundColor: Color,
        override val onClick: () -> Unit = {},
    ) : RoundIconButtonType(
        iconVector = Icons.Rounded.Balance,
        descResource = desc,
        backgroundColor = backgroundColor,
        onClick = onClick,
    )
}

@Composable
private fun RoundIconButton(type: RoundIconButtonType) {
    IconButton(
        modifier = Modifier
            .size(70.dp)
            .background(
                color = type.backgroundColor,
                shape = CircleShape,
            ),
        onClick = { type.onClick() }
    ) {
        when (type) {
            is RoundIconButtonType.Sponsor, is RoundIconButtonType.License -> {
                Icon(
                    modifier = type.offset,
                    imageVector = type.iconVector!!,
                    contentDescription = stringResource(type.descResource!!),
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            }
            is RoundIconButtonType.GitHub, is RoundIconButtonType.Telegram -> {
                Icon(
                    modifier = type.offset,
                    painter = painterResource(type.iconResource!!),
                    contentDescription = type.descString,
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            }
        }
    }
}