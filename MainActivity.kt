package com.shamshadrice.business

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class MainActivity : Activity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var root: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (auth.currentUser == null) login() else dashboard()
    }

    private fun base(title:String): LinearLayout {
        return LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(24,24,24,24)
            addView(TextView(this@MainActivity).apply{text=title;textSize=26f})
        }
    }
    private fun input(h:String, number:Boolean=false)=EditText(this).apply {
        hint=h; if(number) inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
    private fun btn(t:String, f:()->Unit)=Button(this).apply{text=t;setOnClickListener{f()}}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun uid()=auth.currentUser?.uid ?: ""

    private fun login() {
        root=base("SHAMSHAD BUSINESS")
        val email=input("Email"); val pass=input("Password")
        pass.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        root.addView(email);root.addView(pass)
        root.addView(btn("LOGIN"){
            auth.signInWithEmailAndPassword(email.text.toString(),pass.text.toString())
                .addOnSuccessListener{dashboard()}.addOnFailureListener{toast(it.message?:"Login failed")}
        })
        root.addView(btn("CREATE ACCOUNT"){
            auth.createUserWithEmailAndPassword(email.text.toString(),pass.text.toString())
                .addOnSuccessListener{dashboard()}.addOnFailureListener{toast(it.message?:"Signup failed")}
        })
        setContentView(root)
    }

    private fun dashboard() {
        root=base("Dashboard")
        root.addView(btn("📦 Items / Stock"){items()})
        root.addView(btn("🛒 Purchase"){purchase()})
        root.addView(btn("💰 Sale"){sale()})
        root.addView(btn("💵 Paisa Wasool / Cash Received"){paisaWasool()})
        root.addView(btn("👥 Customers"){party("customers")})
        root.addView(btn("🚚 Suppliers"){party("suppliers")})
        root.addView(btn("📊 Reports"){reports()})
        root.addView(btn("🚪 Logout"){auth.signOut();login()})
        setContentView(root)
    }

    private fun items() {
        root=base("Items / Stock")
        val name=input("Item name"); val cat=input("Category"); val unit=input("Unit: kg, bag, piece, liter")
        val qty=input("Opening quantity",true); val buy=input("Purchase rate",true); val sell=input("Sale rate",true)
        root.addView(name);root.addView(cat);root.addView(unit);root.addView(qty);root.addView(buy);root.addView(sell)
        root.addView(btn("SAVE ITEM"){
            val d=hashMapOf("name" to name.text.toString(),"category" to cat.text.toString(),
                "unit" to unit.text.toString(),"quantity" to (qty.text.toString().toDoubleOrNull()?:0.0),
                "purchaseRate" to (buy.text.toString().toDoubleOrNull()?:0.0),
                "saleRate" to (sell.text.toString().toDoubleOrNull()?:0.0),
                "createdAt" to FieldValue.serverTimestamp())
            db.collection("users").document(uid()).collection("items").add(d)
                .addOnSuccessListener{toast("Item saved");items()}.addOnFailureListener{toast(it.message?:"Save failed")}
        })
        root.addView(btn("BACK"){dashboard()});setContentView(root)
    }

    private fun purchase() {
        root=base("Purchase")
        val item=input("Item name");val supplier=input("Supplier");val qty=input("Quantity",true);val rate=input("Purchase rate",true);val paid=input("Paid amount",true)
        listOf(item,supplier,qty,rate,paid).forEach{root.addView(it)}
        root.addView(btn("SAVE PURCHASE"){
            val q=qty.text.toString().toDoubleOrNull()?:0.0;val r=rate.text.toString().toDoubleOrNull()?:0.0
            val total=q*r;val p=paid.text.toString().toDoubleOrNull()?:0.0
            val data=hashMapOf("item" to item.text.toString(),"supplier" to supplier.text.toString(),"quantity" to q,"rate" to r,"total" to total,"paid" to p,"remaining" to total-p,"type" to "purchase","createdAt" to FieldValue.serverTimestamp())
            db.collection("users").document(uid()).collection("transactions").add(data)
                .addOnSuccessListener{findAndAdjustStock(item.text.toString(),q);toast("Purchase saved")}
                .addOnFailureListener{toast(it.message?:"Save failed")}
        })
        root.addView(btn("BACK"){dashboard()});setContentView(root)
    }

    private fun sale() {
        root=base("Sale")
        val item=input("Item name");val customer=input("Customer");val qty=input("Quantity",true);val rate=input("Sale rate",true);val paid=input("Paid amount",true)
        listOf(item,customer,qty,rate,paid).forEach{root.addView(it)}
        root.addView(btn("SAVE SALE"){
            val q=qty.text.toString().toDoubleOrNull()?:0.0;val r=rate.text.toString().toDoubleOrNull()?:0.0
            val total=q*r;val p=paid.text.toString().toDoubleOrNull()?:0.0
            val data=hashMapOf("item" to item.text.toString(),"customer" to customer.text.toString(),"quantity" to q,"rate" to r,"total" to total,"paid" to p,"remaining" to total-p,"type" to "sale","createdAt" to FieldValue.serverTimestamp())
            db.collection("users").document(uid()).collection("transactions").add(data)
                .addOnSuccessListener{findAndAdjustStock(item.text.toString(),-q);toast("Sale saved")}
                .addOnFailureListener{toast(it.message?:"Save failed")}
        })
        root.addView(btn("BACK"){dashboard()});setContentView(root)
    }

    private fun paisaWasool() {
        root=base("Paisa Wasool / Cash Received")
        val customer=input("Customer name")
        val amount=input("Amount received",true)
        val detail=input("Detail (optional)")
        listOf(customer,amount,detail).forEach{root.addView(it)}

        root.addView(btn("SAVE PAISA WASOOL"){
            val c=customer.text.toString().trim()
            val a=amount.text.toString().toDoubleOrNull()?:0.0
            val note=detail.text.toString().trim()

            if(c.isEmpty() || a<=0){
                toast("Customer name aur valid amount enter karein")
                return@btn
            }

            val data=hashMapOf(
                "customer" to c,
                "amount" to a,
                "paid" to a,
                "detail" to note,
                "type" to "receipt",
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(uid()).collection("transactions").add(data)
                .addOnSuccessListener{
                    // Also update the customer's ledger automatically.
                    val q=db.collection("users").document(uid()).collection("customers")
                        .whereEqualTo("name", c).limit(1)
                    q.get().addOnSuccessListener{snap->
                        if(!snap.isEmpty){
                            val doc=snap.documents[0]
                            val current=doc.getDouble("balance")?:0.0
                            doc.reference.update(
                                "balance", current-a,
                                "updatedAt", FieldValue.serverTimestamp()
                            ).addOnCompleteListener{
                                toast("Paisa wasool saved — $c: $a\nKhata balance automatically updated")
                                paisaWasool()
                            }
                        }else{
                            toast("Paisa wasool saved. Customer '$c' not found in Customers, so khata balance was not changed.")
                            paisaWasool()
                        }
                    }.addOnFailureListener{
                        toast("Paisa wasool saved, but khata update failed")
                        paisaWasool()
                    }
                }
                .addOnFailureListener{toast(it.message?:"Save failed")}
        })

        root.addView(btn("BACK"){dashboard()})
        setContentView(root)
    }

    private fun findAndAdjustStock(name:String, delta:Double) {
        db.collection("users").document(uid()).collection("items").whereEqualTo("name",name).limit(1).get()
            .addOnSuccessListener { snap ->
                if(!snap.isEmpty) {
                    val d=snap.documents[0]; val old=(d.getDouble("quantity")?:0.0)
                    d.reference.update("quantity",old+delta)
                }
            }
    }

    private fun party(collection:String) {
        root=base(if(collection=="customers")"Customers" else "Suppliers")
        val name=input("Name");val phone=input("Phone");val address=input("Address")
        listOf(name,phone,address).forEach{root.addView(it)}
        root.addView(btn("SAVE"){
            db.collection("users").document(uid()).collection(collection).add(hashMapOf("name" to name.text.toString(),"phone" to phone.text.toString(),"address" to address.text.toString()))
                .addOnSuccessListener{toast("Saved")}.addOnFailureListener{toast(it.message?:"Save failed")}
        })
        root.addView(btn("BACK"){dashboard()});setContentView(root)
    }

    private fun reports() {
        root=base("Reports")
        val text=TextView(this).apply{text="Loading...";textSize=18f};root.addView(text)
        db.collection("users").document(uid()).collection("transactions").get().addOnSuccessListener{s->
            var sales=0.0;var purchases=0.0;var paidIn=0.0;var paidOut=0.0
            s.forEach{d->
                val type=d.getString("type")
                val total=d.getDouble("total")?:0.0
                val p=d.getDouble("paid")?:0.0
                when(type){
                    "sale" -> { sales+=total; paidIn+=p }
                    "purchase" -> { purchases+=total; paidOut+=p }
                    "receipt" -> { paidIn += (d.getDouble("amount") ?: p) }
                }
            }
            text.text="Sales: $sales\nPurchases: $purchases\nCash received: $paidIn\nCash paid: $paidOut\nGross sales margin estimate: ${sales-purchases}"
        }
        root.addView(btn("BACK"){dashboard()});setContentView(root)
    }
}
