package com.example.exploringexoplayer.ui.audioequaliser

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.example.exploringexoplayer.R
import com.example.exploringexoplayer.data.ExoPlayerManager

var effectType = arrayListOf(
    "Custom", "Flat", "Acoustic", "Dance",
    "Hip Hop", "Jazz", "Pop", "Rock", "Podcast"
)

const val M3U8_URL = "http://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEqualizerView() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.top_app_bar_title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(50, 145, 150, alpha = 150)
    ) {
        val viewModel = hiltViewModel<AudioEqualizerViewModel>()
        val enableEqualizer by viewModel.enableEqualizer.collectAsState()

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            item {
                VideoPlayerView(viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.equalizer_title_text),
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Switch(
                        checked = enableEqualizer, onCheckedChange = {
                            viewModel.toggleEqualizer()
                        }, colors = SwitchDefaults.colors(
                            checkedTrackColor = Color.Black,
                            checkedIconColor = Color.Black,
                            uncheckedTrackColor = Color.White,
                            uncheckedBorderColor = Color.Black,
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                AnimatedVisibility(
                    visible = enableEqualizer,
                    enter = fadeIn() + slideInVertically { fullHeight -> -fullHeight / 2 },
                    exit = fadeOut() + slideOutVertically { fullHeight -> -fullHeight / 3 }
                ) {
                    EqualizerView(viewModel = viewModel)
                }
            }

            item {
                AnimatedVisibility(
                    visible = enableEqualizer,
                    enter = fadeIn() + slideInVertically { fullHeight -> -fullHeight / 2 },
                    exit = fadeOut() + slideOutVertically { fullHeight -> -fullHeight / 2 }
                ) {
                    PresetsView(viewModel)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(viewModel: AudioEqualizerViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember { ExoPlayerManager.getExoPlayer(context) }

    LaunchedEffect(key1 = Unit) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        val uri = Uri.Builder().encodedPath(M3U8_URL).build()
        val mediaItem = MediaItem.Builder().setUri(uri).build()

        val internetVideoSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

        exoPlayer.setMediaSource(internetVideoSource)
        exoPlayer.prepare()

        // Will be used in later implementation for Equalizer
        viewModel.onStart(exoPlayer.audioSessionId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .padding(top = 16.dp)
                .background(Color.Black),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                    exoPlayer.playWhenReady = false
                    useController = true
                }
            }
        )
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exoPlayer.playWhenReady = false
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.playWhenReady = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose { ExoPlayerManager.releaseExoPlayer() }
    }
}

@Composable
fun PresetsView(viewModel: AudioEqualizerViewModel) {
    Column {
        val audioEffects by viewModel.audioEffects.collectAsState()
        val groupedList = effectType.chunked(4)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                thickness = 1.dp
            )

            Text(
                text = stringResource(R.string.presets_title_text),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .wrapContentWidth()
                    .weight(0.5f)
                    .padding(4.dp)
                    .zIndex(1f),
                textAlign = TextAlign.Center
            )

            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        for (itemList in groupedList) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val horizontalPadding =
                    if (maxWidth < 320.dp) 8.dp else if (maxWidth > 400.dp) 40.dp else 20.dp
                val horizontalSpacing = if (maxWidth > 400.dp) 24.dp else 16.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = horizontalSpacing,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (item in itemList) {
                        val index by remember {
                            mutableIntStateOf(
                                effectType.indexOf(
                                    item
                                )
                            )
                        }
                        BoxWithConstraints(
                            modifier = Modifier
                                .wrapContentSize()
                                .border(
                                    1.dp,
                                    if (index == audioEffects?.selectedEffectType) Color.White else Color.Black,
                                    RoundedCornerShape(40.dp)
                                )
                                .clip(RoundedCornerShape(40.dp))
                                .clickable {
                                    viewModel.onSelectPreset(index)
                                }
                                .background(if (index == audioEffects?.selectedEffectType) Color.Black else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(
                                        horizontal = horizontalPadding,
                                        vertical = 12.dp
                                    ),
                                fontSize = 14.sp,
                                color = if (index == audioEffects?.selectedEffectType) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerView(viewModel: AudioEqualizerViewModel) {

    val xAxisLabels = listOf("60Hz", "230Hz", "910Hz", "3kHz", "14kHz")
    val maxLength = xAxisLabels.maxByOrNull { it.length }?.length ?: 0
    val audioEffects by viewModel.audioEffects.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                rotationZ = 270f
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (index in xAxisLabels.indices) {
            Row(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(220.dp)
            ) {
                Box {
                    val paddedLabel = xAxisLabels[index].padStart(maxLength, ' ')
                    Text(
                        text = paddedLabel, modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterStart)
                            .rotate(90f), color = Color.White,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Start
                    )

                    Slider(
                        modifier = Modifier
                            .offset(x = 20.dp),
                        value = audioEffects!!.gainValues[index].times(1000f).toFloat()
                            .coerceIn(-3000f, 3000f),
                        onValueChange = {
                            viewModel.onBandLevelChanged(index, it.toInt())
                        },
                        valueRange = -3000f..3000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Black,
                            activeTrackColor = Color.Black,
                            inactiveTrackColor = Color.White
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        1.dp,
                                        Color.White,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(Color.Black, CircleShape)
                            )
                        }
                    )
                }
            }
        }
    }
}