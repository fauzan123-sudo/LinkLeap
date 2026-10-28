package com.infinity.linkleap.data.module

import com.infinity.linkleap.data.network.FileReceiver
import com.infinity.linkleap.data.network.FileSender
import com.infinity.linkleap.data.repository.NetworkRepository
import com.infinity.linkleap.utils.TransferNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 🧩 Provide FileSender
    @Provides
    @Singleton
    fun provideFileSender(): FileSender {
        return FileSender()
    }

    // 🧩 Provide FileReceiver
    @Provides
    @Singleton
    fun provideFileReceiver(): FileReceiver {
        return FileReceiver()
    }

    // 🧠 Inject keduanya ke NetworkRepository
    @Provides
    @Singleton
    fun provideNetworkRepository(
        sender: FileSender,
        receiver: FileReceiver,
        notifier: TransferNotifier
    ): NetworkRepository {
        return NetworkRepository(sender, receiver, notifier)
    }
}
