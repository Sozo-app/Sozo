package com.animestudios.animeapp.navigation

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.core.content.res.use
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.*



@Navigator.Name("swipeable")
public open class SwipeableFragmentNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator<SwipeableFragmentNavigator.Destination>() {
    private val savedIds = mutableSetOf<String>()

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        Log.i(TAG, "popBackStack()")
        if (fragmentManager.isStateSaved) {
            Log.i(
                TAG, "Ignoring popBackStack() call: FragmentManager has already saved its state"
            )
            return
        }
        if (savedState) {
            val beforePopList = state.backStack.value
            val initialEntry = beforePopList.first()
            // Get the set of entries that are going to be popped
            val poppedList = beforePopList.subList(
                beforePopList.indexOf(popUpTo),
                beforePopList.size
            )
            // Now go through the list in reversed order (i.e., started from the most added)
            // and save the back stack state of each.
            for (entry in poppedList.reversed()) {
                if (entry == initialEntry) {
                    Log.i(
                        TAG,
                        "FragmentManager cannot save the state of the initial destination $entry"
                    )
                } else {
                    fragmentManager.saveBackStack(entry.id)
                    savedIds += entry.id
                }
            }
        } else {
            fragmentManager.popBackStack(
                popUpTo.id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        state.pop(popUpTo, savedState)
    }

    public override fun createDestination(): Destination {
        return Destination(this)
    }

    /**
     * Instantiates the Fragment via the FragmentManager's
     * [androidx.fragment.app.FragmentFactory].
     *
     * Note that this method is **not** responsible for calling
     * [Fragment.setArguments] on the returned Fragment instance.
     *
     * @param context Context providing the correct [ClassLoader]
     * @param fragmentManager FragmentManager the Fragment will be added to
     * @param className The Fragment to instantiate
     * @param args The Fragment's arguments, if any
     * @return A new fragment instance.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        """Set a custom {@link androidx.fragment.app.FragmentFactory} via
      {@link FragmentManager#setFragmentFactory(FragmentFactory)} to control
      instantiation of Fragments."""
    )
    public open fun instantiateFragment(
        context: Context,
        fragmentManager: FragmentManager,
        className: String,
        args: Bundle?
    ): Fragment {
        Log.i(TAG, "instantiateFragment()")
        return fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
    }

    /**
     * {@inheritDoc}
     *
     * This method should always call
     * [FragmentTransaction.setPrimaryNavigationFragment]
     * so that the Fragment associated with the new destination can be retrieved with
     * [FragmentManager.getPrimaryNavigationFragment].
     *
     * Note that the default implementation commits the new Fragment
     * asynchronously, so the new Fragment is not instantly available
     * after this call completes.
     */
    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        Log.i(TAG, "navigate()")
        if (fragmentManager.isStateSaved) {
            Log.i(
                TAG, "Ignoring navigate() call: FragmentManager has already saved its state"
            )
            return
        }
        for (entry in entries) {
            navigate(entry, navOptions, navigatorExtras)
        }
    }

    private fun navigate(
        entry: NavBackStackEntry,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        val backStack = state.backStack.value
        val initialNavigation = backStack.isEmpty()
        val restoreState = (
                navOptions != null && !initialNavigation &&
                        navOptions.shouldRestoreState() &&
                        savedIds.remove(entry.id)
                )
        if (restoreState) {
            // Restore back stack does all the work to restore the entry
            fragmentManager.restoreBackStack(entry.id)
            state.push(entry)
            return
        }
        val destination = entry.destination as Destination
        val args = entry.arguments
        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }
        val frag = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
        frag.arguments = args
        val ft = fragmentManager.beginTransaction()
        var enterAnim = navOptions?.enterAnim ?: -1
        var exitAnim = navOptions?.exitAnim ?: -1
        var popEnterAnim = navOptions?.popEnterAnim ?: -1
        var popExitAnim = navOptions?.popExitAnim ?: -1
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = if (enterAnim != -1) enterAnim else 0
            exitAnim = if (exitAnim != -1) exitAnim else 0
            popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
            popExitAnim = if (popExitAnim != -1) popExitAnim else 0
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }
        ft.add(containerId, frag)
        ft.setPrimaryNavigationFragment(frag)
        @IdRes val destId = destination.id
        // TODO Build first class singleTop behavior for fragments
        val isSingleTopReplacement = (
                navOptions != null && !initialNavigation &&
                        navOptions.shouldLaunchSingleTop() &&
                        backStack.last().destination.id == destId
                )
        val isAdded = when {
            initialNavigation -> {
                ft.addToBackStack(entry.id)
                true
            }
            isSingleTopReplacement -> {
                // Single Top means we only want one instance on the back stack
                if (backStack.size > 1) {
                    // If the Fragment to be replaced is on the FragmentManager's
                    // back stack, a simple replace() isn't enough so we
                    // remove it from the back stack and put our replacement
                    // on the back stack in its place
                    fragmentManager.popBackStack(
                        entry.id,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    ft.addToBackStack(entry.id)
                }
                false
            }
            else -> {
                ft.addToBackStack(entry.id)
                true
            }
        }
        Log.i(TAG, "isAdded $isAdded")
        if (navigatorExtras is Extras) {
            for ((key, value) in navigatorExtras.sharedElements) {
                ft.addSharedElement(key, value)
            }
        }
        ft.setReorderingAllowed(true)
        ft.commit()
        // The commit succeeded, update our view of the world
        if (isAdded) {
            state.push(entry)
        }
    }

    public override fun onSaveState(): Bundle? {
        if (savedIds.isEmpty()) {
            return null
        }
        return bundleOf(KEY_SAVED_IDS to ArrayList(savedIds))
    }

    public override fun onRestoreState(savedState: Bundle) {
        val savedIds = savedState.getStringArrayList(KEY_SAVED_IDS)
        if (savedIds != null) {
            this.savedIds.clear()
            this.savedIds += savedIds
        }
    }

    @NavDestination.ClassType(Fragment::class)
    public open class Destination
    public constructor(fragmentNavigator: Navigator<out Destination>) :
        NavDestination(fragmentNavigator) {

        public constructor(navigatorProvider: NavigatorProvider) :
                this(navigatorProvider.getNavigator(SwipeableFragmentNavigator::class.java))

        @CallSuper
        public override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)
            context.resources.obtainAttributes(attrs, androidx.navigation.fragment.R.styleable.FragmentNavigator).use { array ->
                val className = array.getString(androidx.navigation.fragment.R.styleable.FragmentNavigator_android_name)
                if (className != null) setClassName(className)
            }
        }

        public fun setClassName(className: String): Destination {
            _className = className
            return this
        }

        private var _className: String? = null

        public val className: String
            get() {
                checkNotNull(_className) { "Fragment class was not set" }
                return _className as String
            }

        public override fun toString(): String {
            val sb = StringBuilder()
            sb.append(super.toString())
            sb.append(" class=")
            if (_className == null) {
                sb.append("null")
            } else {
                sb.append(_className)
            }
            return sb.toString()
        }

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is Destination) return false
            return super.equals(other) && _className == other._className
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + _className.hashCode()
            return result
        }
    }

    public class Extras internal constructor(sharedElements: Map<View, String>) :
        Navigator.Extras {
        private val _sharedElements = LinkedHashMap<View, String>()

        public val sharedElements: Map<View, String>
            get() = _sharedElements.toMap()

        public class Builder {
            private val _sharedElements = LinkedHashMap<View, String>()

            public fun addSharedElements(sharedElements: Map<View, String>): Builder {
                for ((view, name) in sharedElements) {
                    addSharedElement(view, name)
                }
                return this
            }

            public fun addSharedElement(sharedElement: View, name: String): Builder {
                _sharedElements[sharedElement] = name
                return this
            }

            public fun build(): Extras {
                return Extras(_sharedElements)
            }
        }

        init {
            _sharedElements.putAll(sharedElements)
        }
    }

    private companion object {
        private const val TAG = "SwipeableFragmentNavigator"
        private const val KEY_SAVED_IDS = "androidx-nav-fragment:swipeable-navigator:savedIds"
    }
}