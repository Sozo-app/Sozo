package com.animestudios.animeapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.util.AttributeSet
import android.view.*
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Scroller
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.response.Genre
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.activity.MainActivity
import com.animestudios.animeapp.ui.screen.search.dialog.tab.model.FilterTabModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


var statusBarHeight = 0
var navBarHeight = 0
val Int.dp: Float get() = (this / Resources.getSystem().displayMetrics.density)
val Int.dpIndicator: Float get() = (this * (Resources.getSystem().displayMetrics.densityDpi / 160f))
var selectedPosition = -1

val Float.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
const val EMPTY_VALUE_STRING = "~"
const val EMPTY_VALUE_INT = 0
const val EMPTY_VALUE_FLOAT = 0F

lateinit var bottomBar: BottomNavigationView

fun <T> randomSelectFromList(list: List<T>): T? {
    if (Anilist.adult) {
        return list[Random().nextInt(list.size)]
    } else {
        val filteredList = list.filter { it != "HENTAI" }
        if (filteredList.isEmpty()) {
            return null
        }
        return filteredList[Random().nextInt(filteredList.size)]
    }

}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return tryWith {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cap = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return@tryWith if (cap != null) {
                when {
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_USB) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> true
                    else -> false
                }
            } else false
        } else true
    } ?: false
}

object Refresh {
    fun all() {
        for (i in activity) {
            activity[i.key]!!.postValue(true)
        }
    }

    val activity = mutableMapOf<Int, MutableLiveData<Boolean>>()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}


fun localLoadTabTxt(): ArrayList<String> {
    val list = ArrayList<String>()
    list.add("All Anime")
    list.add("Genres")
    return list
}

fun loadDetailTabs(): ArrayList<String> {
    val list = ArrayList<String>()
    list.add("Episodes")
    list.add("Cast")
    list.add("Relations")
    list.add("Details")
    return list
}


fun loadFilterTab(
    defaultItemGenre: ArrayList<String>,
    defaultItemSort: String,
    defaultItemYears: String
): ArrayList<FilterTabModel> {
    val list = ArrayList<FilterTabModel>()
    list.add(FilterTabModel(defaultItemGenre, "Genre", ""))


    val newList: MutableList<String> = ArrayList()
    Anilist.anime_formats.onEach {
        newList.add(it.toString())
    }
    list.add(FilterTabModel(newList, "Format", defaultItemYears))


    list.add(
        FilterTabModel(
            currContext()!!.resources.getStringArray(R.array.sort_by).toMutableList(),
            "Sort",
            defaultItemSort
        )
    )

    return list
}


class ZoomOutPageTransformer(private val uiSettings: UISettings) :
    ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        if (position == 0.0f && uiSettings.layoutAnimations) {
            setAnimation(
                view.context,
                view,
                uiSettings,
                300,
                floatArrayOf(1.3f, 1f, 1.3f, 1f),
                0.5f to 0f
            )
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f)
                .setDuration((200 * uiSettings.animationSpeed).toLong()).start()
        }
    }
}

fun currContext(): Context? {
    return App.currentContext()
}

fun <T> readData(fileName: String, context: Context? = null, toast: Boolean = true): T? {
    val a = context ?: currContext()
    try {
        if (a?.fileList() != null)
            if (fileName in a.fileList()) {
                val fileIS: FileInputStream = a.openFileInput(fileName)
                val objIS = ObjectInputStream(fileIS)
                val data = objIS.readObject() as T
                objIS.close()
                fileIS.close()
                return data
            }
    } catch (e: Exception) {
        if (toast) snackString("Error loading data $fileName")
        e.printStackTrace()
    }
    return null
}

fun currActivity(): Activity? {
    return App.currentActivity()
}

var selectedOption = 0


fun snackString(s: String?, activity: Activity? = null, clipboard: String? = null) {
    if (s != null) {
        (activity ?: currActivity())?.apply {
            runOnUiThread {
                val snackBar = Snackbar.make(
                    window.decorView.findViewById(android.R.id.content),
                    s,
                    Snackbar.LENGTH_LONG
                )
                snackBar.setBackgroundTint(currActivity()!!.getColor(R.color.dark_500))
                snackBar.view.apply {
                    updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                        width = ViewGroup.LayoutParams.WRAP_CONTENT

                    }
                    translationY = -(navBarHeight.dp + 32f)
                    translationZ = 32f
                    updatePadding(
                        16f.px, right = 16f.px, bottom = 5f.px
                    )
                    setOnClickListener {
                        snackBar.dismiss()
                    }
                    setOnLongClickListener {
                        copyToClipboard(clipboard ?: s, false)
                        toast("Copied to Clipboard")
                        true
                    }
                }
                snackBar.show()
            }
        }
        logger(s)
    }
}

fun copyToClipboard(string: String, toast: Boolean = true) {
    val activity = currContext() ?: return
    val clipboard = ContextCompat.getSystemService(activity, ClipboardManager::class.java)
    val clip = ClipData.newPlainText("label", string)
    clipboard?.setPrimaryClip(clip)
    if (toast) snackString("Copied \"$string\"")
}


fun toast(string: String?) {
    if (string != null) {
        logger(string)
        MainScope().launch {
            Toast.makeText(currActivity()?.application ?: return@launch, string, Toast.LENGTH_SHORT)
                .show()
        }
    }
}


fun logger(e: Any?, print: Boolean = true) {
    if (print)
        println(e)
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

class SafeClickListener(
    private var defaultInterval: Int = 800,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}


fun <T> tryWith(post: Boolean = false, snackbar: Boolean = true, call: () -> T): T? {
    return try {
        call.invoke()
    } catch (e: Throwable) {
        null
    }
}

fun saveData(fileName: String, data: Any?, context: Context? = null) {
    tryWith {
        val a = context ?: currContext()
        if (a != null) {
            val fos: FileOutputStream = a.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(data)
            os.close()
            fos.close()
        }
    }
}

class CardTransformer(private val context: Context) : ViewPager2.PageTransformer {
    private val nextItemVisiblePx = 36.dp
    private val currentItemHorizontalMarginPx = 16.dp
    private val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx

    override fun transformPage(page: View, position: Float) {
        val displayMetrics = context.resources.displayMetrics
        val dpHeight = displayMetrics.heightPixels / displayMetrics.density

        page.translationX = -pageTranslationX * position  /*dpHeight/(100)*/
        page.scaleY = 1 - (0.12f * kotlin.math.abs(position))
        page.alpha = 0.5f + (1 - kotlin.math.abs(position))
    }

}

class MediaPageTransformer : ViewPager2.PageTransformer {
    private fun parallax(view: View, position: Float) {
        if (position > -1 && position < 1) {
            val width = view.width.toFloat()
            view.translationX = -(position * width * 0.8f)
        }
    }

    override fun transformPage(view: View, position: Float) {

        val bannerContainer = view.findViewById<View>(R.id.itemCompactBanner)
        parallax(bannerContainer, position)
    }
}


fun ImageView.loadImage(url: String?, size: Int = 0) {
    if (!url.isNullOrEmpty()) {
        loadImage(FileUrl(url), size)
    }
}

fun ImageView.loadImage(file: FileUrl?, size: Int = 0) {
    if (file?.url?.isNotEmpty() == true) {
        tryWith {
            val glideUrl = GlideUrl(file.url) { file.headers }
            Glide.with(this.context).load(glideUrl)
                .transition(DrawableTransitionOptions.withCrossFade()).override(size).into(this)
        }
    }
}


var loaded: Boolean = false


fun loadIcons():ArrayList<Int>{
    val list = ArrayList<Int>()
    list.add(com.animestudios.animeapp.R.drawable.ic_bookmark)
    list.add(R.drawable.ic_heart)
    list.add(R.drawable.ic_share)
    list.add(R.drawable.ic_close)
    list.add(R.drawable.ic_network)
    return list
}

fun View.preventTwoClick() {
    this.isEnabled = false
    this.postDelayed({
        isEnabled = true
    }, 300)
}


fun String.toFirstUpperCase(): String {
    val kattaHarf = this[0].toUpperCase()  // Birinchi harfni katta qilish
    val qolganQism = this.substring(1).toLowerCase()  // Qolgan qismni olib tashlash
    val yangiSoz = kattaHarf + qolganQism  // Yangi sozni yaratish
    return yangiSoz
}


fun Activity.hideStatusBar() {
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


fun openLinkInBrowser(link: String?) {
    tryWith {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        currContext()?.startActivity(intent)
    }
}


fun initActivity(a: Activity) {
    val window = a.window
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val uiSettings = readData<UISettings>("ui_settings", toast = false)

        ?: UISettings().apply {
            saveData("ui_settings", this)
        }
    uiSettings.darkMode.apply {
        AppCompatDelegate.setDefaultNightMode(
            when (this) {
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
    if (uiSettings.immersiveMode) {
        if (navBarHeight == 0) {
            ViewCompat.getRootWindowInsets(window.decorView.findViewById(android.R.id.content))
                ?.apply {
                    navBarHeight = this.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                }
        }
        a.hideStatusBar()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && statusBarHeight == 0 && a.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            window.decorView.rootWindowInsets?.displayCutout?.apply {
                if (boundingRects.size > 0) {
                    statusBarHeight = min(boundingRects[0].width(), boundingRects[0].height())
                }
            }
        }
    } else
        if (statusBarHeight == 0) {
            val windowInsets =
                ViewCompat.getRootWindowInsets(window.decorView.findViewById(android.R.id.content))
            if (windowInsets != null) {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                statusBarHeight = insets.top
                navBarHeight = insets.bottom
            }
        }


}

class CustomViewPagerScroll : ViewPager {

    constructor(context: Context) : super(context) {
        postInitViewPager()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        postInitViewPager()
    }

    private var mScroller: ScrollerCustomDuration? = null

    private fun postInitViewPager() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            val interpolator = viewpager.getDeclaredField("sInterpolator")
            interpolator.isAccessible = true

            mScroller = ScrollerCustomDuration(context, interpolator.get(null) as Interpolator)
            scroller.set(this, mScroller)
        } catch (_: Exception) {
        }

    }


    fun setScrollDurationFactor(scrollFactor: Int) {
        mScroller!!.setScrollDurationFactor(scrollFactor)
    }

    inner class ScrollerCustomDuration(context: Context, interpolator: Interpolator) :
        Scroller(context, interpolator) {

        private var mScrollFactor = 1

        fun setScrollDurationFactor(scrollFactor: Int) {
            mScrollFactor = scrollFactor
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, (duration * mScrollFactor))
        }

    }

}

var loadMedia: Int? = null

fun startMainActivity(activity: Activity, bundle: Bundle? = null) {
    activity.finishAffinity()
    activity.startActivity(
        Intent(
            activity,
            MainActivity::class.java
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            if (bundle != null) putExtras(bundle)
        }
    )
}

fun parallaxPageTransformer(vararg v: Int): ViewPager.PageTransformer =
    ViewPager.PageTransformer { page, position ->
        for (element in v) {
            val view = page.findViewById<View>(element)
            if (view != null) ViewCompat.setTranslationX(
                view,
                (page.width / 1.5 * position).toFloat()
            )
        }
    }

fun pageTransformer(): ViewPager.PageTransformer = ViewPager.PageTransformer { page, position ->
    page.translationX = if (position < 0.0f) 0.0f else (-page.width).toFloat() * position
}

fun setGenreItemAnimation(activity: Activity?, view: View) {
    val animator1: ObjectAnimator = ObjectAnimator.ofFloat(view, "translationX", -200f)
    animator1.setRepeatCount(0)
    animator1.setDuration(1000)

    val animator2 = ObjectAnimator.ofFloat(view, "translationX", 100f)
    animator2.repeatCount = 0
    animator2.duration = 1000

    val animator3 = ObjectAnimator.ofFloat(view, "translationX", 0f)
    animator3.repeatCount = 0
    animator3.duration = 1000

//sequencial animation

//sequencial animation
    val set = AnimatorSet()
    set.play(animator1).before(animator2)
    set.play(animator2).before(animator3)
    set.start()

}

fun displayInDayDateTimeFormat(seconds: Int): String {
    val dateFormat = SimpleDateFormat("E, dd MMM yyyy, hh:mm a", Locale.getDefault())
    val date = Date(seconds * 1000L)
    return dateFormat.format(date)
}


fun setAnimation(
    context: Context,
    viewToAnimate: View,
    uiSettings: UISettings,
    duration: Long = 150,
    list: FloatArray = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f),
    pivot: Pair<Float, Float> = 0.5f to 0.5f
) {
    if (uiSettings.layoutAnimations) {
        val anim = ScaleAnimation(
            list[0],
            list[1],
            list[2],
            list[3],
            Animation.RELATIVE_TO_SELF,
            pivot.first,
            Animation.RELATIVE_TO_SELF,
            pivot.second
        )
        anim.duration = (duration * uiSettings.animationSpeed).toLong()
        anim.setInterpolator(context, R.anim.over_shoot)
        viewToAnimate.startAnimation(anim)
    }
}

fun setSlideIn(uiSettings: UISettings) = AnimationSet(false).apply {
    if (uiSettings.layoutAnimations) {
        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = (500 * uiSettings.animationSpeed).toLong()
        animation.interpolator = AccelerateDecelerateInterpolator()
        addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0f
        )

        animation.duration = (750 * uiSettings.animationSpeed).toLong()
        animation.interpolator = OvershootInterpolator(1.1f)
        addAnimation(animation)
    }
}

fun setSlideUp(uiSettings: UISettings) = AnimationSet(false).apply {
    if (uiSettings.layoutAnimations) {
        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = (500 * uiSettings.animationSpeed).toLong()
        animation.interpolator = AccelerateDecelerateInterpolator()
        addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0f
        )

        animation.duration = (750 * uiSettings.animationSpeed).toLong()
        animation.interpolator = OvershootInterpolator(1.1f)
        addAnimation(animation)
    }
}

fun MutableMap<String, Genre>.checkId(id: Int): Boolean {
    this.forEach {
        if (it.value.id == id) {
            return false
        }
    }
    return true
}

fun MutableMap<String, Genre>.checkGenreTime(genre: String): Boolean {
    if (containsKey(genre))
        return (System.currentTimeMillis() - get(genre)!!.time) >= (1000 * 60 * 60 * 24 * 7)
    return true
}


abstract class GesturesListener : GestureDetector.SimpleOnGestureListener() {
    private var timer: Timer? = null //at class level;
    private val delay: Long = 200

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        processSingleClickEvent(e)
        return super.onSingleTapUp(e)
    }

    override fun onLongPress(e: MotionEvent) {
        processLongClickEvent(e)
        super.onLongPress(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        processDoubleClickEvent(e)
        return super.onDoubleTap(e)
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        onScrollYClick(distanceY)
        onScrollXClick(distanceX)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    private fun processSingleClickEvent(e: MotionEvent) {
        val handler = Handler(Looper.getMainLooper())
        val mRunnable = Runnable {
            onSingleClick(e)
        }
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post(mRunnable)
                }
            }, delay)
        }
    }

    private fun processDoubleClickEvent(e: MotionEvent) {
        timer?.apply {
            cancel()
            purge()
        }
        onDoubleClick(e)
    }

    private fun processLongClickEvent(e: MotionEvent) {
        timer?.apply {
            cancel()
            purge()
        }
        onLongClick(e)
    }

    open fun onSingleClick(event: MotionEvent) {}
    open fun onDoubleClick(event: MotionEvent) {}
    open fun onScrollYClick(y: Float) {}
    open fun onScrollXClick(y: Float) {}
    open fun onLongClick(event: MotionEvent) {}
}

fun View.circularReveal(ex: Int, ey: Int, subX: Boolean, time: Long) {
    ViewAnimationUtils.createCircularReveal(
        this,
        if (subX) (ex - x.toInt()) else ex,
        ey - y.toInt(),
        0f,
        max(height, width).toFloat()
    ).setDuration(time).start()
}


class FadingEdgeRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun isPaddingOffsetRequired(): Boolean {
        return !clipToPadding
    }

    override fun getLeftPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingLeft
    }

    override fun getTopPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingTop
    }

    override fun getRightPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingRight
    }

    override fun getBottomPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingBottom
    }


}

fun View.alphaAnim() {
    val anim = AnimationUtils.loadAnimation(
        currContext()!!,
        R.anim.alpha_anim
    ).apply {
        duration = 700L

        fillAfter = true
    }

    startAnimation(anim)

}

fun View.fadeOut() {
    val anim = AnimationUtils.loadAnimation(
        currContext()!!,
        R.anim.fade_out
    ).apply {
        duration = 700L

        fillAfter = true
    }

    startAnimation(anim)

}

class MyCountDownTimer(startTime: Long, interval: Long, private val func: () -> Unit) :
    CountDownTimer(startTime, interval) {
    override fun onFinish() = func()
    override fun onTick(timer: Long) {}
}

enum class MediaStatusAnimity {
    COMPLETED,
    WATCHING,
    DROPPED,
    PAUSED,
    PLANNING,
    REPEATING,
    NOTHING;

    companion object {
        fun stringToMediaListStatus(passedString: String?): MediaStatusAnimity {
            return when (passedString?.uppercase(Locale.getDefault())) {
                "COMPLETED" -> COMPLETED
                "CURRENT" -> WATCHING
                "DROPPED" -> DROPPED
                "PAUSED" -> PAUSED
                "PLANNING" -> PLANNING
                "REPEATING" -> REPEATING
                else -> NOTHING
            }
        }
    }
}


