package com.animestudios.animeapp.utils

import com.animestudios.animeapp.model.UserStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// PresenceManager.kt
class PresenceManager(private val db: FirebaseDatabase) {
  fun start(myUserId: Int) {
    val statusNode = db.getReference("status").child(myUserId.toString())
    val connRef    = db.getReference(".info/connected")
    connRef.addValueEventListener(object: ValueEventListener {
      override fun onDataChange(snap: DataSnapshot) {
        val connected = snap.getValue(Boolean::class.java) ?: false
        if (connected) {
          // on disconnect set offline + lastSeen
          statusNode.onDisconnect()
            .setValue(UserStatus(online=false, lastSeen=System.currentTimeMillis()))
          // now go online
          statusNode.setValue(UserStatus(online=true, lastSeen=System.currentTimeMillis()))
        }
      }
      override fun onCancelled(err: DatabaseError) {}
    })
  }
}