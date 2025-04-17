package com.animestudios.animeapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.anilist.response.Genre
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.databinding.ItemCountDownBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.model.ProfileCategoryModel
import com.animestudios.animeapp.model.ThemeModel
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.client
import com.animestudios.animeapp.tools.convertFromSnakeCase
import com.animestudios.animeapp.tools.tryWithSuspend
import com.animestudios.animeapp.type.MediaType
import com.animestudios.animeapp.ui.activity.MainActivity
import com.animestudios.animeapp.ui.screen.search.dialog.tab.model.FilterTabModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


fun Fragment.applyColorByAttr(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = requireContext().theme
    theme.resolveAttribute(
        attrColor, typedValue,
        true
    )
    @ColorInt val selectedcolor: Int = typedValue.data

    val typedValueBg = TypedValue()
    val themeBg: Resources.Theme = requireContext().theme
    themeBg.resolveAttribute(
        attrColor, typedValueBg,
        true
    )

    return selectedcolor
}


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

fun loadThemes(): ArrayList<ThemeModel> {
    val list = ArrayList<ThemeModel>()
    list.add(
        ThemeModel(
            R.color.md_theme_dark_1_primary,
            R.color.md_theme_dark_1_colorControlNormal,
            "BLUE"
        )
    )

    list.add(
        ThemeModel(
            R.color.md_theme_dark_2_primary,
            R.color.md_theme_dark_2_colorControlNormal,
            "GREEN"
        )
    )
    list.add(
        ThemeModel(
            R.color.md_theme_dark_3_primary,
            R.color.md_theme_dark_3_colorControlNormal,
            "PURPLE"
        )
    )
    list.add(
        ThemeModel(
            R.color.md_theme_dark_4_primary,
            R.color.md_theme_dark_4_colorControlNormal,
            "PINK"
        )
    )

    list.add(
        ThemeModel(
            R.color.md_theme_dark_5_primary,
            R.color.md_theme_dark_1_colorControlNormal,
            "SAIKOU"
        )
    )
    list.add(
        ThemeModel(R.color.basic_color, R.color.banner_bg, "RED")
    )

    list.add(
        ThemeModel(
            R.color.md_theme_dark_7_primary,
            R.color.md_theme_dark_7_colorControlNormal,
            "LAVENDER"
        )
    )
    list.add(
        ThemeModel(
            R.color.md_theme_dark_8_primary,
            R.color.md_theme_dark_8_colorControlNormal,
            "OCEAN"
        )
    )
    list.add(
        ThemeModel(
            R.color.md_theme_dark_9_primary,
            R.color.md_theme_dark_9_colorControlNormal,
            "YELLOW"
        )
    )
    return list
}

@SuppressLint("SetTextI18n")


fun MediaType.getString(): String {
    return name.convertFromSnakeCase(false)
}


fun loadProfileCategory(): ArrayList<ProfileCategoryModel> {
    val list = ArrayList<ProfileCategoryModel>()
    list.add(
        ProfileCategoryModel(
            "Appearance",
            "Adjust the app to your liking",
            R.drawable.appearance
        )
    )
    list.add(
        ProfileCategoryModel(
            "Anime Source",
            "Change Source according to you",
            R.drawable.ic_profile_item
        )
    )
    list.add(
        ProfileCategoryModel(
            "Player",
            "Player appearance , playback...",
            R.drawable.ic_player
        )
    )
    list.add(
        ProfileCategoryModel(
            "Anilist Settings",
            "Change your anilist setting",
            R.drawable.anilistlogo
        )
    )

    list.add(
        ProfileCategoryModel(
            "About",
            "FAQ , Contact Developer, About App",
            R.drawable.ic_about
        )
    )


    return list


}

fun countDown(media: Media, view: ViewGroup) {
    if (media.anime?.nextAiringEpisode != null && media.anime.nextAiringEpisodeTime != null && (media.anime.nextAiringEpisodeTime!! - System.currentTimeMillis() / 1000) <= 86400 * 7.toLong()) {
        val v = ItemCountDownBinding.inflate(LayoutInflater.from(view.context), view, false)
        view.addView(v.root, 0)
        v.mediaCountdownText.text =
            "Episode ${media.anime.nextAiringEpisode!! + 1} will be released in"
        object : CountDownTimer(
            (media.anime.nextAiringEpisodeTime!! + 10000) * 1000 - System.currentTimeMillis(),
            1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val a = millisUntilFinished / 1000
                v.mediaCountdown.text =
                    "${a / 86400} days ${a % 86400 / 3600} hrs ${a % 86400 % 3600 / 60} mins ${a % 86400 % 3600 % 60} secs"
            }

            override fun onFinish() {
                v.mediaCountdownContainer.visibility = View.GONE
                snackString("Congrats Vro")
            }
        }.start()
    }
}

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


class PopImageButton(
    private val scope: CoroutineScope,
    private val image: ImageView,
    private val d1: Int,
    private val d2: Int,
    private val c1: Int,
    private val c2: Int,
    var clicked: Boolean,
    callback: suspend (Boolean) -> (Unit)
) {
    private var disabled = false
    private val context = image.context
    private var pressable = true

    init {
        enabled(true)
        scope.launch {
            clicked()
        }
        image.setOnClickListener {
            if (pressable && !disabled) {
                pressable = false
                clicked = !clicked
                scope.launch {
                    launch(Dispatchers.IO) {
                        callback.invoke(clicked)
                    }
                    clicked()
                    pressable = true
                }
            }
        }
    }

    suspend fun clicked() {
        if (clicked) {
            ObjectAnimator.ofArgb(
                image,
                "ColorFilter",
                ContextCompat.getColor(context, c1)
            ).setDuration(120).start()
            image.setImageDrawable(AppCompatResources.getDrawable(context, d1))
        } else image.setImageDrawable(AppCompatResources.getDrawable(context, d2))
        if (clicked) ObjectAnimator.ofArgb(
            image,
            "ColorFilter",
            ContextCompat.getColor(context, c1)
        ).setDuration(200).start()
    }

    fun enabled(enabled: Boolean) {
        disabled = !enabled
        image.alpha = if (disabled) 0.33f else 1f
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
    list.add("Statistics")
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

/**
 * Extracts a “vibrant” color from this Bitmap (or falls back to [defaultColor]).
 */
fun Bitmap.getVibrantColor(defaultColor: Int): Int {
    val palette = Palette.from(this).generate()
    return palette.vibrantSwatch?.rgb
        ?: palette.dominantSwatch?.rgb
        ?: defaultColor
}

/**
 * Asynchronously extracts a color from this ImageView’s drawable (if it’s a BitmapDrawable),
 * then sets “[name][suffix]” on the TextView, coloring only the [name] portion.
 *
 * @param name the user name to highlight
 * @param suffix the rest of the message (default “ followed you”)
 * @param defaultHighlightRes a @ColorRes fallback in case Palette fails
 */
fun TextView.setColoredNameFromImage(
    name: String,
    suffix: String = "",
    imageView: ImageView,
    @ColorRes defaultHighlightRes: Int
) {
    // grab the Bitmap from the ImageView’s drawable
    val bd = imageView.drawable
    val bmp = (bd as? android.graphics.drawable.BitmapDrawable)?.bitmap
    if (bmp == null) {
        // no bitmap? just use default color span
        setColoredName(name, suffix, defaultHighlightRes)
        return
    }

    // generate Palette asynchronously
    Palette.from(bmp).generate { palette ->
        val defaultColor = ContextCompat.getColor(context, defaultHighlightRes)
        val color = palette?.vibrantSwatch?.rgb
            ?: palette?.dominantSwatch?.rgb
            ?: defaultColor

        // build and apply spannable
        val full = "$name$suffix"
        val span = SpannableString(full)
        span.setSpan(
            ForegroundColorSpan(color),
            0,
            name.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = span
    }
}

 fun Fragment.getThemeColor(@AttrRes attr: Int): Int {
    val tv = TypedValue()
    requireContext().theme.resolveAttribute(attr, tv, true)
    return tv.data
}

/**
 * Synchronous fallback: uses a fixed @ColorRes for the name span.
 */
fun TextView.setColoredName(
    name: String,
    suffix: String = " followed you",
    @ColorRes nameColorRes: Int
) {
    val full = "$name$suffix"
    val span = SpannableString(full)
    val color = ContextCompat.getColor(context, nameColorRes)
    span.setSpan(
        ForegroundColorSpan(color),
        0,
        name.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    text = span
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
var loadedBrowse: Boolean = false


fun loadIcons(): ArrayList<Int> {
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
    val kattaHarf = this[0].uppercaseChar()  // Birinchi harfni katta qilish
    val qolganQism =
        this.substring(1).lowercase(Locale.getDefault())  // Qolgan qismni olib tashlash
    val yangiSoz = kattaHarf + qolganQism  // Yangi sozni yaratish
    return yangiSoz
}


fun Activity.hideStatusBar() {
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

@Suppress("DEPRECATION")
fun Activity.hideSystemBars() {
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
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
    animator1.repeatCount = 0
    animator1.duration = 1000

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

fun updateAnilistProgress(media: Media, number: String) {
    if (Anilist.userid != null) {
        CoroutineScope(Dispatchers.IO).launch {
            val a = number.toFloatOrNull()?.roundToInt()
            if (a != media.userProgress) {
//                Anilist.editList(
//                    media.id,
//                    a,
//                    status = if (media.userStatus == "REPEATING") media.userStatus else "CURRENT"
//                )
            }
            media.userProgress = a
            Refresh.all()
        }
    } else {
        toast("Please Login into anilist account!")
    }
}

fun String.findBetween(a: String, b: String): String? {
    val start = this.indexOf(a)
    val end = if (start != -1) this.indexOf(b, start) else return null
    return if (end != -1) this.subSequence(start, end).removePrefix(a).removeSuffix(b)
        .toString() else null
}

suspend fun getSize(file: FileUrl): Double? {
    return tryWithSuspend {
        client.head(file.url, file.headers, timeout = 1000).size?.toDouble()?.div(1024 * 1024)
    }
}

suspend fun getSize(file: String): Double? {
    return getSize(FileUrl(file))
}

open class BottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onStart() {
        super.onStart()
        if (this.resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            val behavior = BottomSheetBehavior.from(requireView().parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}

// Function to clear cookies in a WebView
fun clearCookies(webView: WebView) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
    // Reload the WebView after clearing cookies if needed
    webView.reload()
}

@SuppressWarnings("deprecation")
fun clearCookies(context: Context?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    } else if (context != null) {
        val cookieSyncManager = CookieSyncManager.createInstance(context)
        cookieSyncManager.startSync()
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookie()
        cookieManager.removeSessionCookie()
        cookieSyncManager.stopSync()
        cookieSyncManager.sync()
    }
}


fun getCurrentBrightnessValue(context: Context): Float {
    fun getMax(): Int {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val fields: Array<Field> = powerManager.javaClass.declaredFields
        for (field in fields) {
            if (field.name.equals("BRIGHTNESS_ON")) {
                field.isAccessible = true
                return try {
                    field.get(powerManager)?.toString()?.toInt() ?: 255
                } catch (e: IllegalAccessException) {
                    255
                }
            }
        }
        return 255
    }

    fun getCur(): Float {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            127
        ).toFloat()
    }

    return brightnessConverter(getCur() / getMax(), true)
}

fun brightnessConverter(it: Float, fromLog: Boolean) =
    MathUtils.clamp(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            if (fromLog) log2((it * 256f)) * 12.5f / 100f else 2f.pow(it * 100f / 12.5f) / 256f
        else it, 0.001f, 1f
    )


fun checkCountry(context: Context): Boolean {
    val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return when (telMgr.simState) {
        TelephonyManager.SIM_STATE_ABSENT -> {
            val tz = TimeZone.getDefault().id
            tz.equals("Asia/Kolkata", ignoreCase = true)
        }

        TelephonyManager.SIM_STATE_READY -> {
            val countryCodeValue = telMgr.networkCountryIso
            countryCodeValue.equals("in", ignoreCase = true)
        }

        else -> false
    }
}

open class NoPaddingArrayAdapter<T>(context: Context, layoutId: Int, items: List<T>) :
    ArrayAdapter<T>(context, layoutId, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
        (view as TextView).setTextColor(Color.WHITE)
        return view
    }
}
