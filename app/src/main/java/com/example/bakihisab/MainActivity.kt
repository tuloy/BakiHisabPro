package com.example.bakihisab

import android.app.*
import android.os.Bundle
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.*
import android.widget.*
import android.text.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class Payment(val id:Long,val amount:Double,val date:String)
data class Person(val id:Long,var name:String,var total:Double,val payments:MutableList<Payment>){
 fun paid()=payments.sumOf{it.amount}
 fun remaining()=(total-paid()).coerceAtLeast(0.0)
}

class MainActivity:Activity(){
 private val people=mutableListOf<Person>()
 private val prefs by lazy{getSharedPreferences("baki_pro",MODE_PRIVATE)}
 private lateinit var list:LinearLayout; private lateinit var search:EditText
 private lateinit var summary:TextView

 override fun onCreate(b:Bundle?){super.onCreate(b);load();ui();render()}

 private fun ui(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,18,14,10);setBackgroundColor(Color.rgb(244,247,251))}
  val title=TextView(this).apply{text="💰  বাকি হিসাব Pro";textSize=27f;setTextColor(Color.rgb(24,34,48))}
  root.addView(title)
  summary=TextView(this).apply{textSize=15f;setPadding(14,14,14,14);setBackgroundColor(Color.WHITE)}
  root.addView(summary,lp(-1,80))
  val form=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  val n=EditText(this).apply{hint="নাম (যেমন: Josef)";singleLine=true}
  val a=EditText(this).apply{hint="মোট পাওনা";inputType=2;singleLine=true}
  val add=Button(this).apply{text="➕ যোগ"}
  form.addView(n,weight());form.addView(a,weight());form.addView(add,weight());root.addView(form)
  add.setOnClickListener{val name=n.text.toString().trim();val total=a.text.toString().toDoubleOrNull()
   if(name.isBlank()||total==null||total<0) toast("নাম ও সঠিক টাকা দিন")
   else{people.add(Person(System.currentTimeMillis(),name,total,mutableListOf()));save();n.text.clear();a.text.clear();render()}}
  search=EditText(this).apply{hint="🔍 নাম দিয়ে খুঁজুন";singleLine=true}
  root.addView(search,lp(-1,55));search.addTextChangedListener(object:TextWatcher{
   override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){render()};override fun afterTextChanged(e:Editable?){} })
  val buttons=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  val save=Button(this).apply{text="💾 Save"};val csv=Button(this).apply{text="📄 CSV"};val excel=Button(this).apply{text="📊 Excel"}
  buttons.addView(save,weight());buttons.addView(csv,weight());buttons.addView(excel,weight());root.addView(buttons)
  save.setOnClickListener{save();toast("হিসাব Save হয়েছে ✅")}
  csv.setOnClickListener{share(csv())};excel.setOnClickListener{share(csv())}
  val sc=ScrollView(this);list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};sc.addView(list);root.addView(sc,lp(-1,0,1f))
  setContentView(root)
 }

 private fun render(){
  list.removeAllViews();val q=search.text.toString().trim().lowercase()
  people.filter{it.name.lowercase().contains(q)}.forEach{p->card(p)}
  val due=people.sumOf{it.total};val paid=people.sumOf{it.paid()}
  summary.text="মোট পাওনা: ${money(due)}    •    পরিশোধ: ${money(paid)}    •    বাকি: ${money((due-paid).coerceAtLeast(0.0))}"
 }
 private fun card(p:Person){
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,12,14,12);setBackgroundColor(Color.WHITE)}
  val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  val t=TextView(this).apply{text="${p.name}  |  ${money(p.total)}";textSize=18f;setTextColor(Color.rgb(24,34,48))}
  val edit=Button(this).apply{text="✏ Edit"};row.addView(t,weight());row.addView(edit,lp(95,-2));box.addView(row)
  val info=TextView(this).apply{text="পরিশোধ: ${money(p.paid())}   |   বাকি: ${money(p.remaining())}";textSize=16f;setPadding(0,7,0,7)}
  box.addView(info)
  val pay=Button(this).apply{text="💵 পরিশোধ যোগ / History"}
  box.addView(pay)
  pay.setOnClickListener{paymentDialog(p)}
  edit.setOnClickListener{editDialog(p)}
  list.addView(box,lp(-1,-2).apply{setMargins(0,0,0,9)})
 }
 private fun editDialog(p:Person){
  val lay=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(30,10,30,0)}
  val n=EditText(this).apply{setText(p.name);hint="নাম"};val a=EditText(this).apply{setText(p.total.toString());inputType=2}
  lay.addView(n);lay.addView(a)
  AlertDialog.Builder(this).setTitle("Edit").setView(lay).setNegativeButton("বাতিল",null).setPositiveButton("Save"){_,_->p.name=n.text.toString().trim();p.total=a.text.toString().toDoubleOrNull()?:p.total;save();render()}.show()
 }
 private fun paymentDialog(p:Person){
  val lay=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(25,5,25,0)}
  val amount=EditText(this).apply{hint="আজ কত টাকা দিয়েছে?";inputType=2};lay.addView(amount)
  val history=TextView(this).apply{setPadding(0,15,0,0);text=if(p.payments.isEmpty())"কোনো payment history নেই" else p.payments.joinToString("\n"){it.date+" — "+money(it.amount)}}
  lay.addView(history)
  AlertDialog.Builder(this).setTitle("${p.name} — Payment History").setView(lay).setNegativeButton("বন্ধ",null).setPositiveButton("Payment Save"){_,_->
   val v=amount.text.toString().toDoubleOrNull()?:0.0
   if(v>0){p.payments.add(Payment(System.currentTimeMillis(),v,java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(Date())));save();render()}}.show()
 }
 private fun save(){val ar=JSONArray();people.forEach{p->ar.put(JSONObject().apply{put("id",p.id);put("name",p.name);put("total",p.total);put("payments",JSONArray().apply{p.payments.forEach{x->put(JSONObject().apply{put("id",x.id);put("amount",x.amount);put("date",x.date)})}})})};prefs.edit().putString("data",ar.toString()).apply()}
 private fun load(){val ar=JSONArray(prefs.getString("data","[]"));for(i in 0 until ar.length()){val o=ar.getJSONObject(i);val ps=mutableListOf<Payment>();val pa=o.optJSONArray("payments")?:JSONArray();for(j in 0 until pa.length()){val x=pa.getJSONObject(j);ps.add(Payment(x.getLong("id"),x.getDouble("amount"),x.getString("date")))};people.add(Person(o.getLong("id"),o.getString("name"),o.getDouble("total"),ps))}}
 private fun csv():String{val sb=StringBuilder("Name,Total,Paid,Remaining\n");people.forEach{sb.append("${it.name},${it.total},${it.paid()},${it.remaining()}\n")};return sb.toString()}
 private fun share(text:String){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/csv";putExtra(Intent.EXTRA_TEXT,text)},"Export করুন"))}
 private fun money(x:Double)="৳"+String.format(Locale.US,"%.2f",x).removeSuffix(".00")
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
 private fun lp(w:Int,h:Int,weight:Float=0f)=LinearLayout.LayoutParams(w,h,weight)
 private fun weight()=LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(3,0,3,0)}
}