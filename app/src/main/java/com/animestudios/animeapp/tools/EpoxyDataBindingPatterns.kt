package com.animestudios.animeapp.tools

import com.airbnb.epoxy.EpoxyDataBindingPattern
import com.animestudios.animeapp.R

/* A class annotation that tells Epoxy to generate a binding class for each layout file that matches
the pattern. */
@EpoxyDataBindingPattern(rClass = R::class, layoutPrefix = "item")
object EpoxyDataBindingPatterns