package me.ash.reader.ui.page.settings.tips

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.TipsAndUpdates
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.ui.component.base.CurlyCornerShape
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.theme.palette.alwaysLight
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsAndSupportPage(
    navController: NavHostController,
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val view = LocalView.current
    var currentVersion by remember { mutableStateOf("") }
    var clickTime by remember { mutableStateOf(System.currentTimeMillis() - 2000) }
    var pressAMP by remember { mutableStateOf(16f) }
    val animatedPress by animateFloatAsState(
        targetValue = pressAMP,
        animationSpec = tween()
    )

    LaunchedEffect(Unit) {
        currentVersion = context.getCurrentVersion().toString()
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        actions = {
            FeedbackIconButton(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Rounded.Balance,
                contentDescription = stringResource(R.string.open_source_licenses),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                context.showToast(context.getString(R.string.coming_soon))
            }
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
                                    if (System.currentTimeMillis() - clickTime > 2000) {
                                        clickTime = System.currentTimeMillis()
                                        updateViewModel.checkUpdate(
                                            {
                                                context.showToast(context.getString(R.string.checking_updates))
                                                context.dataStore.put(
                                                    DataStoreKeys.SkipVersionNumber,
                                                    ""
                                                )
                                            },
                                            {
                                                if (!it) {
                                                    context.showToast(
                                                        context.getString(R.string.is_latest_version)
                                                    )
                                                }
                                            }
                                        )
                                    } else {
                                        clickTime = System.currentTimeMillis()
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
                                painter = painterResource(R.drawable.ic_launcher_pure),
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
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.showToast(context.getString(R.string.coming_soon))
                        })
                        Spacer(modifier = Modifier.width(16.dp))

                        // GitHub
                        RoundIconButton(RoundIconButtonType.GitHub(
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.openURL(context.getString(R.string.github_link), OpenLinkPreference.AutoPreferCustomTabs)
                        })
                        Spacer(modifier = Modifier.width(16.dp))

                        // Telegram
                        RoundIconButton(RoundIconButtonType.Telegram(
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.openURL(context.getString(R.string.telegram_link), OpenLinkPreference.AutoPreferCustomTabs)
                        })
                        Spacer(modifier = Modifier.width(16.dp))

                        // Help
                        RoundIconButton(RoundIconButtonType.Help(
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer alwaysLight true,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            context.showToast(context.getString(R.string.coming_soon))
                        })
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    )

    UpdateDialog()
}

@Immutable
sealed class RoundIconButtonType(
    val iconResource: Int? = null,
    val iconVector: ImageVector? = null,
    val descResource: Int? = null,
    val descString: String? = null,
    open val size: Dp = 24.dp,
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
    data class Help(
        val desc: Int = R.string.help,
        override val offset: Modifier = Modifier.offset(x = (3).dp),
        override val backgroundColor: Color,
        override val onClick: () -> Unit = {},
    ) : RoundIconButtonType(
        iconVector = Icons.Rounded.TipsAndUpdates,
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
            is RoundIconButtonType.Sponsor, is RoundIconButtonType.Help -> {
                Icon(
                    modifier = type.offset.size(type.size),
                    imageVector = type.iconVector!!,
                    contentDescription = stringResource(type.descResource!!),
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            }

            is RoundIconButtonType.GitHub, is RoundIconButtonType.Telegram -> {
                Icon(
                    modifier = type.offset.size(type.size),
                    painter = painterResource(type.iconResource!!),
                    contentDescription = type.descString,
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            }
        }
    }
}
