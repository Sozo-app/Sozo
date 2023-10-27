package com.animestudios.animeapp.widget

typealias OnNeedToRequestPermissions = (permissions: Array<String>) -> Unit


interface PermissionsFeature {

    /**
     * A callback invoked when permissions need to be requested by the feature before
     * it can complete its task. Once the request is completed, [onPermissionsResult]
     * needs to be invoked.
     */
    val onNeedToRequestPermissions: OnNeedToRequestPermissions

    /**
     * Notifies the feature that a permission request was completed.
     * The feature should react to this and complete its task.
     *
     * @param permissions The permissions that were granted.
     * @param grantResults The grant results for the corresponding permission
     */
    fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray)
}
