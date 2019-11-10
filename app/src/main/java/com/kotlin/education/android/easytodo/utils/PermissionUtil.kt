package com.kotlin.education.android.easytodo.utils

import android.Manifest
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import androidx.core.app.ActivityCompat
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


/**
 * The utility for checking permissions. Since Android 6.0, google introduces runtime permissions.
 * This means, that we need to ask a user for the permission when a scecific permission is needed.
 * https://developer.android.com/training/permissions/requesting
 */
class PermissionUtil {

    companion object {

        /**
         * For Each permission, we use a different method.
         */
        fun requestReadStoragePermission(context: AppCompatActivity, requestCode: Int) {
            requestPermissions(context, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        fun checkReadStoragePermission(context: AppCompatActivity): Boolean
                = checkPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE)


        fun requestCameraStoragePermission(context: AppCompatActivity, requestCode: Int) {
            requestPermissions(context, requestCode, Manifest.permission.CAMERA)
        }

        fun checkCameraStoragePermission(context: AppCompatActivity): Boolean
                = checkPermissions(context, Manifest.permission.CAMERA)

        /**
         * Checks permission
         * @param context context
         * @return returns true if permission is granted
         */
        private fun checkPermissions(context: AppCompatActivity, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }


        /**
         * Requests permission from the user.
         * @param context context
         * @param requestCode the permission request code.
         */
        private fun requestPermissions(context: Activity, requestCode: Int, permission: String) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(permission),
                requestCode
            )

        }
    }

}