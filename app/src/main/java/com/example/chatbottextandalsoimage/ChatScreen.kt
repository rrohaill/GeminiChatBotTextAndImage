package com.example.chatbottextandalsoimage

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.chatbottextandalsoimage.viewmodel.ChatViewModel
import dev.rrohaill.chatbotexample.R

@Composable
fun ChatScreen(paddingValues: PaddingValues) {
    val chatViewModel = viewModel<ChatViewModel>()
    val chatState = chatViewModel.chatState.collectAsState().value
    var uriState by rememberSaveable { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                uriState = uri.toString()

            }
        }
    )
    val bitmap = getBitmap(uriState)
    Crossfade(chatState.chatList, label = "ChatList") { chatList ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(MaterialTheme.colorScheme.secondaryContainer),
            verticalArrangement = Arrangement.Bottom,

            ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                itemsIndexed(chatList) { _, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(
                            prompt = chat.prompt,
                            bitmap = chat.bitmap
                        )
                    } else {
                        ModelChatItem(response = chat.prompt)
                    }
                }

            }
            Column {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Image",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(35.dp)
                                .align(Alignment.CenterVertically)
                                .padding(top = 5.dp)
                        )
                    }

                    TextField(
                        value = chatState.prompt,
                        onValueChange = {
                            chatViewModel.onEvent(ChatUiEvent.UpdatePrompt(it))
                        },
                        label = { Text(text = stringResource(R.string.input_message_title)) },
                        placeholder = { Text(text = stringResource(R.string.input_message_prompt)) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp)),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            chatViewModel.onEvent(
                                ChatUiEvent.SendPrompt(
                                    chatState.prompt,
                                    bitmap
                                )
                            )
                            uriState = ""
                        })
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    //Send Icon
                    Icon(
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.CenterVertically)
                            .padding(top = 5.dp, end = 5.dp)
                            .clickable {
                                chatViewModel.onEvent(
                                    ChatUiEvent.SendPrompt(
                                        chatState.prompt,
                                        bitmap
                                    )
                                )
                                uriState = ""
                            },
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send prompt",
                        tint = MaterialTheme.colorScheme.primary
                    )

                }
                AnimatedVisibility(visible = uriState.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            bitmap?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .requiredSize(50.dp)
                                        .padding(top = 5.dp)
                                )
                            }
                            TextButton(onClick = { uriState = "" }) {
                                Text(text = "Remove")
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun UserChatItem(prompt: String, bitmap: Bitmap?) {
    Column(
        modifier = Modifier
            .padding(start = 100.dp, bottom = 22.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        bitmap?.let {
            Image(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(260.dp)
                    .padding(bottom = 2.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                bitmap = it.asImageBitmap()
            )
        }

        Text(
            textAlign = TextAlign.End,
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
            text = prompt,
            fontSize = 17.sp,
            color = Color.Black,
        )
    }
}

@Composable
fun ModelChatItem(response: String) {
    Column(modifier = Modifier.padding(end = 100.dp, bottom = 22.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFBF2))
                .padding(16.dp),
            text = response,
            fontSize = 17.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun getBitmap(uriState: String): Bitmap? {
    val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
        model = ImageRequest.Builder(
            LocalContext.current
        )
            .data(uriState)
            .size(Size.ORIGINAL)
            .build()
    ).state

    if (imageState is AsyncImagePainter.State.Success) {
        return imageState.result.drawable.toBitmap()
    }
    return null
}