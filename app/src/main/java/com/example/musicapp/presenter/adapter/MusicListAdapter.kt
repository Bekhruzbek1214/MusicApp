package com.example.musicapp.presenter.adapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.ItemMusicBinding
import com.example.musicapp.utils.selectMusicPos
import java.io.File

class MusicListAdapter : ListAdapter<MusicData, MusicListAdapter.MusicHolder>(MusicDiffUtil) {

    private var onClickMusic: ((MusicData) -> Unit)? = null
    private var oldSelectedMusicPos: Int = -1
    private var playingState = false

    private var time = 0L

    object MusicDiffUtil : DiffUtil.ItemCallback<MusicData>() {
        override fun areItemsTheSame(oldItem: MusicData, newItem: MusicData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MusicData, newItem: MusicData): Boolean {
            return oldItem == newItem
        }

    }

    inner class MusicHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (System.currentTimeMillis() - time >= 500) {
                    time = System.currentTimeMillis()
                    selectMusicPos = absoluteAdapterPosition
                    onClickMusic?.invoke(
                        getItem(absoluteAdapterPosition)
                    )
                }
            }
        }

        fun bind() {
            getItem(absoluteAdapterPosition)?.apply {
                val file = File(musicPath)
                if (file.exists()) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(imagePath)
//                    val coverBytes = retriever.embeddedPicture
//                    if (coverBytes != null) {
//                        val bitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
//                        Glide
//                            .with(binding.root.context)
//                            .load(bitmap)
//                            .into(binding.musicImg)
//                    } else {
//                        binding.musicImg.setImageResource(R.drawable.music_circle)
//                    }
//                } else {
//                    binding.musicImg.setImageResource(R.drawable.music_circle)
//                }
                    if (absoluteAdapterPosition == selectMusicPos) {
                       // binding.lottie.isGone = false
                      //  binding.musicName.setTextColor(binding.root.context.getColor(R.color.app_active_color))
                        //binding.musicNumber.foreground = ColorDrawable(Color.parseColor("#6F000000"))
                        if(absoluteAdapterPosition == 9){
                            binding.musicNumber.text = "10"
                        }else if(absoluteAdapterPosition.toString().length == 1){
                            binding.musicNumber.text = "0${absoluteAdapterPosition+1}"
                        }
                        else{
                            binding.musicNumber.text = "${absoluteAdapterPosition+1}"
                        }
                        binding.imgLottie.isVisible = true

                    } else {
                        binding.imgLottie.isVisible = false
                       if(absoluteAdapterPosition == 9){
                            binding.musicNumber.text = "10"
                       }else if(absoluteAdapterPosition.toString().length == 1){
                            binding.musicNumber.text = "0${absoluteAdapterPosition+1}"
                       }
                       else{
                           binding.musicNumber.text = "${absoluteAdapterPosition+1}"
                        }

                    }

                    binding.musicName.text = title
                    binding.artist.text = artist
                }
            }
        }


        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MusicHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        override fun onBindViewHolder(holder: MusicHolder, position: Int) {
            holder.bind()
        }

        fun setOnClickMusic(onClickMusic: ((MusicData) -> Unit)) {
            this.onClickMusic = onClickMusic
        }

        fun playingChange() {

            if (oldSelectedMusicPos != -1) {
                notifyItemChanged(oldSelectedMusicPos)
            }
            if (selectMusicPos != -1) {
                notifyItemChanged(selectMusicPos)
            }
            oldSelectedMusicPos = selectMusicPos
        }

        fun setManageState(state: Boolean) {
            playingState = state
            notifyItemChanged(selectMusicPos)
        }


    }






