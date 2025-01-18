package com.example.kedaiku.UI.penjualan_menu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.kedaiku.R;
import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.Cash; // Import Cash entity
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.helper.PdfHelper;
import com.example.kedaiku.repository.SaleRepository;
import com.example.kedaiku.viewmodel.CartViewModel;
import com.example.kedaiku.viewmodel.SaleViewModel;
import com.example.kedaiku.viewmodel.WholesaleViewModel;
import com.example.kedaiku.viewmodel.SpecialPriceViewModel;
import com.example.kedaiku.viewmodel.CashViewModel; // Import CashViewModel

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PenjualanKasirActivity extends AppCompatActivity {


    private LinearLayout itemsContainer;
    private TextView textViewSelectedCustomer;
    private EditText editTextTransactionName;
    private ActivityResultLauncher<Intent> customerSelectLauncher;

    private ActivityResultLauncher<Intent> pilihProdukLauncher;
    private WholesaleViewModel wholesaleViewModel; // ViewModel untuk mengakses data grosir
    private SpecialPriceViewModel specialPriceViewModel; // ViewModel untuk mengakses data spesial
    private CashViewModel cashViewModel; // ViewModel untuk mengakses data kas
    private CartViewModel cartViewModel;
    private SaleViewModel saleViewModel;


    // Map untuk menyimpan harga terendah
  //  private Map<Long, PriceInfo> lowestPriceMap;
    double total=0;
    double ongkir=0;
    double potongan=0;
    double subtotal=0;
    long selectedDate;
    Customer curentCustomer;
    long customerId;
    private long customerGroupId;
    private long selectedCashId=0;
    private String selectedCashName;

    // UI Elements
    private EditText editTextShippingCostInput;
    private EditText editTextDiscountInput;
    private EditText editTextPaid;
    private TextView textViewShippingCost;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private TextView textViewPaidFormatted;
    private TextView textViewChange;
    private TextView textViewLabelChange;// TextView for change
    private Spinner spinnerKas;
    private double paidAmount=0;
    private Button buttonChooseCustomer,buttonSave,buttonClearAll,buttonAddItem;
    private TextView textViewSubTotal;
    private Map<Long, CartItem> cartItemsMap;
    private Map<Long, CartItem> oldCartItemsMap;

    SimpleDateFormat dateFormat;
    String formattedDate;
    private long paymentType=0;
    private double hpp=0;

    JSONObject saleDetailJson = new JSONObject();
    JSONObject promoDetailJson = new JSONObject();
    JSONArray itemsArray = new JSONArray();
    JSONArray promoItemsArray= new JSONArray();
    private boolean isEditMode;
    private boolean isEditInit;

    private long saleId,promoId,detailSaleId;
    private EditText editTextDate;

    private ActivityResultLauncher<Intent> createPdfLauncher;
    // Launcher untuk proses "Share PDF"
    private ActivityResultLauncher<Intent> sharePdfLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_kasir);
        cartItemsMap = new HashMap<>();
        oldCartItemsMap = new HashMap<>();

        ///init viewmodel

        wholesaleViewModel = new ViewModelProvider(this).get(WholesaleViewModel.class); // Inisialisasi ViewModel untuk grosir
        specialPriceViewModel = new ViewModelProvider(this).get(SpecialPriceViewModel.class); // Inisialisasi ViewModel untuk harga spesial
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class); // Inisialisasi ViewModel untuk kas
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        saleViewModel = new ViewModelProvider(this).get(SaleViewModel.class);


        ///init view
        editTextTransactionName = findViewById(R.id.editTextTransactionName);
        editTextDate = findViewById(R.id.editTextDate);
        editTextShippingCostInput = findViewById(R.id.editTextShippingCostInput);
        editTextDiscountInput = findViewById(R.id.editTextDiscountInput);
        editTextPaid = findViewById(R.id.editTextPaid);
        textViewShippingCost = findViewById(R.id.textViewShippingCost);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewSubTotal = findViewById(R.id.textViewSubtotal);
        textViewPaidFormatted = findViewById(R.id.textViewPaidFormatted);
        textViewChange = findViewById(R.id.textViewChange);
        textViewLabelChange = findViewById(R.id.textViewChangeAmount);
        spinnerKas = findViewById(R.id.spinnerKas);
        itemsContainer = findViewById(R.id.itemsContainer);
        textViewSelectedCustomer = findViewById(R.id.textViewSelectedCustomer);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // ...

        saleId = getIntent().getLongExtra("sale_id", -1);
        promoId = getIntent().getLongExtra("promo_id", -1);
        detailSaleId = getIntent().getLongExtra("detailSale_id", -1);

        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        isEditInit = getIntent().getBooleanExtra("edit_init", false);
      //  Toast.makeText(this,isEditMode+" "+saleId,Toast.LENGTH_SHORT).show();

        ///tanggal
        // Set tanggal hari ini di editTextDate
        selectedDate =  System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
         dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        formattedDate = dateFormat.format(selectedDate);
        editTextDate.setText(formattedDate);

        // Set nama transaksi dengan tanggal dan waktu saat ini
        SimpleDateFormat transactionNameFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        editTextTransactionName.setText("Penjualan : Tgl " + transactionNameFormat.format(calendar.getTime()));


        // Initialize the ActivityResultLauncher
        pilihProdukLauncher = registerForActivityResult
                (
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        long productId = data.getLongExtra("product_id", -1);
                        String productName = data.getStringExtra("product_name");
                        String unit = data.getStringExtra("product_unit");
                        double price = data.getDoubleExtra("product_price", -1.0);
                        double productQty = data.getDoubleExtra("product_stock", -1.0);
                        double quantity = data.getDoubleExtra("quantity", -1);
                        double hpp = data.getDoubleExtra("product_HPP", -1);

                        if (productId != -1 && productName != null) {
                            addItemView(productId, productName, price, productQty, quantity,hpp, unit);
                        }
                    }
                }
        );

        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonAddItem.setOnClickListener(v -> launchPilihProdukActivity());

        //clearAll
        buttonClearAll = findViewById(R.id.buttonClearAll);
        buttonClearAll.setOnClickListener(v -> {
            cartViewModel.removeAllCartItems();

        });

        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> {

            CompleteTransaction();


        });


        ///customer
        buttonChooseCustomer = findViewById(R.id.buttonChooseCustomer);
        buttonChooseCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(PenjualanKasirActivity.this, CustomerSelectActivity.class);
            customerSelectLauncher.launch(intent);
        });

        // Set awal customer
        curentCustomer = new Customer("Umum", "", "", "", 0);
        curentCustomer.setId(0);
        customerId = curentCustomer.getId();
        customerGroupId = curentCustomer.getCustomer_group_id();
        textViewSelectedCustomer.setText("Nama Pelanggan: " + curentCustomer.getCustomer_name());


        customerSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        customerId = result.getData().getLongExtra("customer_id", -1);
                        customerGroupId = result.getData().getLongExtra("customer_groupId", -1);
                        curentCustomer.setId(customerId);
                        curentCustomer.setCustomer_group_id(customerGroupId);
                        String customerName = result.getData().getStringExtra("customer_name");
                        curentCustomer.setCustomer_name(customerName);
//

                        cartViewModel.setCurrentCustomer(curentCustomer);

                    } else {
                        Toast.makeText(this, "Tidak ada pelanggan yang dipilih", Toast.LENGTH_SHORT).show();
                    }
                }
        );



        ///Ongkir
        updateShippingCost();
        // Set up TextWatcher for shipping cost
        editTextShippingCostInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateShippingCost();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        ///Potongan
        updateDiscount();
        // Set up TextWatcher for discount
        editTextDiscountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateDiscount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        ///uang yg dibayar
        // Set up TextWatcher for paid amount
        editTextPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePaidAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        /// Load cash options into spinner

        loadCashOptions();



        // Observasi perubahan pada cartItemsMap
        cartViewModel.getCartItemsMap().observe(this, cartItems -> {
            cartItemsMap = cartItems;

            Log.d("bandingkan old dan new",oldCartItemsMap.size()+" : "+cartItemsMap.size());

            updateUI(cartItemsMap); // Perbarui tampilan dengan data baru
        });

        // Observasi perubahan pada currentCustomer
        cartViewModel.getCurrentCustomer().observe(this, customer -> {

          //  Toast.makeText(this,customer.getCustomer_name()+" observe ",Toast.LENGTH_SHORT).show();
                curentCustomer = customer;
                customerGroupId=customer.getCustomer_group_id();
                customerId=customer.getId();

            textViewSelectedCustomer.setText("Nama Pelanggan: " + curentCustomer.getCustomer_name());

                updateSpecialPrice(customerGroupId);


        });
        cartViewModel.getCurrentDate().observe(this,
                date->{

            selectedDate = date;
                     dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDate);
                    editTextDate.setText(formattedDate);

                    SimpleDateFormat jammenitdetik = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                          editTextTransactionName.setText("Penjualan : Tgl " + formattedDate+" "+jammenitdetik.format(calendar.getTime()));

                }
                );

        if(isEditMode){

            cartViewModel.getOldCartItemsMap().observe(this, cartItems -> {
                oldCartItemsMap = cartItems;
              //  Toast.makeText(PenjualanKasirActivity.this,"masuk Old",Toast.LENGTH_SHORT).show();

            });
        }



        if (isEditInit && saleId != -1) {

            Observer<SaleWithDetails> saleObserver = new Observer<SaleWithDetails>() {
                @Override
                public void onChanged(SaleWithDetails saleWithDetails) {
                    if (saleWithDetails != null) {
                      //  Toast.makeText(PenjualanKasirActivity.this,"masuk sini 339",Toast.LENGTH_SHORT).show();
                        // 2. Isi form dengan data penjualan lama
                        fillFormWithOldData(saleWithDetails);
                        // 3. Hapus observer setelah data diterima
                        saleViewModel.getSaleWithDetailsByIdLive(saleId).removeObserver(this);
                    }
                }
            };
            saleViewModel.getSaleWithDetailsByIdLive(saleId).observe(this, saleObserver);

        }

        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    List<CartItem> cartItemList = new ArrayList<>(cartItemsMap.values());
                    if (result.getResultCode() == RESULT_OK) {
                        Intent dataResult = result.getData();
                        if (dataResult != null) {
                            Uri uri = dataResult.getData();
                            if (uri != null) {
                                //Log.d(TAG, "URI dipilih: " + uri.toString());
                                // Membuat PDF menggunakan PdfHelper
                                boolean success = PdfHelper.createPdf(this,
                                        uri,
                                        editTextTransactionName.getText().toString(),
                                        editTextDate.getText().toString(),
                                        textViewSelectedCustomer.getText().toString(),
                                        textViewSubTotal.getText().toString(),
                                        textViewShippingCost.getText().toString(),
                                        textViewDiscount.getText().toString(),
                                        textViewTotal.getText().toString(),
                                        textViewLabelChange.getText().toString(),
                                        textViewChange.getText().toString(),
                                        cartItemList
                                );

                                if (success) {
                                    // Setelah berhasil membuat PDF, bagikan PDF
                                    sharePdf(uri);
                                }
                            } else {
                              //  Log.e(TAG, "URI kosong");
                                Toast.makeText(this, "Gagal mendapatkan lokasi penyimpanan PDF.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                       // Log.e(TAG, "Hasil dari pembuatan dokumen dibatalkan");
                        Toast.makeText(this, "Pembuatan PDF dibatalkan.", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        // 2. Buat launcher untuk proses "Share PDF"
        sharePdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Setelah proses share (atau user batal), kita tutup Activity
                    finish();
                }
        );

    }//onCreate

    private void fillFormWithOldData(SaleWithDetails saleWithDetails) {
        // 1. Ambil objek lama
        Sale oldSale          = saleWithDetails.getSale();
        DetailSale oldDetail  = saleWithDetails.getDetailSale();
        //PromoDetail oldPromo  = saleWithDetails.getPromo();

        // 2. Isi UI: Tanggal, Nama Transaksi
        long oldSaleDate = oldSale.getSale_date();
        selectedDate = oldSaleDate; // update variabel

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oldSaleDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(cal.getTime());

        //iki gk masalah
        editTextDate.setText(formattedDate);

        editTextTransactionName.setText(oldSale.getSale_transaction_name());
////


        // 3. Isi Ongkir (shipping cost), Diskon, Paid
        double oldShip = oldSale.getSale_ship();
        double oldDiscount = oldSale.getSale_discount();
        double oldPaid = oldSale.getSale_paid();
        double oldTotal = oldSale.getSale_total();

        //iki gk msalah
        editTextShippingCostInput.setText(String.valueOf(oldShip));
        editTextDiscountInput.setText(String.valueOf(oldDiscount));
        editTextPaid.setText(String.valueOf(oldPaid));
        //

        // 4. Payment Type (jika logika: paid < total => piutang(2), else cash(1))
        if (oldPaid < oldTotal) {
            paymentType = 2; // Piutang
            textViewLabelChange.setText("Piutang");
        } else {
            paymentType = 1; // Cash
            textViewLabelChange.setText("Kembalian");
        }



        // 6. Set Cash (Spinner)
        selectedCashId = oldSale.getSale_cash_id();

        LiveData<List<Cash>> ldCash = cashViewModel.getAllCash();

        ldCash.observe(this, new Observer<List<Cash>>() {
            @Override
            public void onChanged(List<Cash> cashList) {
             //   Toast.makeText(PenjualanKasirActivity.this,"load cash 1 nya",Toast.LENGTH_SHORT).show();
                // Populate spinner with cash options
               for(int i=0;i<cashList.size();i++)
               {
                   if(cashList.get(i).getId()==selectedCashId) {
                       spinnerKas.setSelection(i,false);
                       break;
                   }
               }
               ldCash.removeObserver(this);
            }
        });




        // 7. Parse DetailSale => cartItemsMap => set ke cartViewModel
        String oldJsonDetail = oldDetail.getSale_detail();

        List<CartItem> oldCartItems = FormatHelper.parseCartItemsFromDetailSale(oldJsonDetail,saleWithDetails.getPromo().getDetail());
        Map<Long, CartItem> newMap = new HashMap<>();
        for (CartItem ci : oldCartItems) {

            newMap.put(ci.getProductId(), ci);
            oldCartItemsMap.put(ci.getProductId(),ci.clone());

        }
        cartItemsMap = newMap;

        cartViewModel.setCartItemsMap(cartItemsMap);

        cartViewModel.setOldCartItemsMap(oldCartItemsMap);


        updateShippingCost();
        updateDiscount();
        updatePaidAmount();

        customerId = oldSale.getSale_customer_id();
        if (customerId == 0) {

            curentCustomer.setCustomer_group_id(0);
            curentCustomer.setCustomer_name("Umum");
        }
        else {
            if(saleWithDetails.getCustomer()==null){
                curentCustomer.setCustomer_name("Pelanggan terhapus");
            }
            else{
                curentCustomer = saleWithDetails.getCustomer();
            }
        }
       cartViewModel.setCurrentCustomer(curentCustomer);
       getIntent().putExtra("edit_init",false);
    }



    private void CompleteTransaction() {
        if (selectedCashId == 0) {
            Toast.makeText(this, "Pilih cash yang valid.", Toast.LENGTH_SHORT).show();
            return;
        }



       Sale sale = new Sale(selectedDate,editTextTransactionName.getText().toString(),curentCustomer.getId(),-1,
               paymentType,total,selectedCashId,hpp,potongan,ongkir,-1,paidAmount);

        // Membuat objek JSON untuk sale detail
        JSONObject salePaidHistorylJson = new JSONObject();
        JSONArray itemsArray = new JSONArray();

        try {
            // Membuat item pertama
            JSONObject item1 = new JSONObject();
            item1.put("date", selectedDate);
            item1.put("paid", paidAmount);
           // Menambahkan item pertama ke array
            itemsArray.put(item1);

            // Menambahkan array items ke objek sale_detail
            salePaidHistorylJson.put("paid_history", itemsArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DetailSale ds= new DetailSale(saleDetailJson.toString());
        ds.setSale_paid_history(salePaidHistorylJson.toString());
        PromoDetail pd = new PromoDetail(promoDetailJson.toString());
        List<CartItem> items = new ArrayList<>(this.cartItemsMap.values());

      // saleViewModel.processSale(sale,ds,pd,items);

        if(paymentType==2 &&(customerGroupId==0||customerGroupId==-1)||cartItemsMap.size()<1)
        {
                Toast.makeText
                        (this,"Barang masih kosong atau\nPelanggan Umum Gk Boleh Utang",
                                Toast.LENGTH_SHORT).show();
        }
        else {

            if(!isEditMode){

            // Panggil metode completeTransaction di ViewModel dengan listener
            saleViewModel.processSale(sale, ds, pd, items, new SaleViewModel.OnTransactionCompleteListener() {
                @Override
                public void onSuccess(boolean status) {

                        Toast.makeText(PenjualanKasirActivity.this, "Transaksi berhasil!", Toast.LENGTH_SHORT).show();
                    //createPdfWithSAF();
                    //finish(); // Menutup activity
                    new AlertDialog.Builder(PenjualanKasirActivity.this)
                            .setMessage("Apakah ingin mencetak dan share nota?")
                            .setPositiveButton("Ya", (dialog, which) -> {
                                // User ingin mencetak & share nota -> kita panggil createPdfWithSAF()
                                createPdfWithSAF();
                            })
                            .setNegativeButton("Tidak", (dialog, which) -> {
                                // User menolak -> kita langsung tutup Activity
                                finish();
                            })
                            .show();

                }

                @Override
                public void onFailure(boolean status) {
                    runOnUiThread(() -> {
                        Toast.makeText(PenjualanKasirActivity.this, "Transaksi gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
            else{
                sale.setId(saleId);
                sale.setPromo_id(promoId);
                sale.setSale_detail_id(detailSaleId);
                saleViewModel.updateSaleTransaction(sale,ds.getSale_detail(),pd.getDetail(),items,new SaleViewModel.OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(boolean status) {


                        Toast.makeText(PenjualanKasirActivity.this, "Transaksi Update berhasil!", Toast.LENGTH_SHORT).show();
                        finish(); // Menutup activity

                    }

                    @Override
                    public void onFailure(boolean status) {
                        runOnUiThread(() -> {
                            Toast.makeText(PenjualanKasirActivity.this, "Transaksi update gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                        });
                    }
                });


            }

        }

    }

    private void updateUI( Map<Long, CartItem> cartItemsMap) {

        subtotal=0; 
        hpp=0;
        itemsArray = new JSONArray();
        promoItemsArray = new JSONArray();
        saleDetailJson = new JSONObject();
        promoDetailJson = new JSONObject();
       // Toast.makeText(this,"update ui"+cartItemsMap.size(),Toast.LENGTH_SHORT).show();

        itemsContainer.removeAllViews();

        try{

        for (Map.Entry<Long, CartItem> entry : cartItemsMap.entrySet()) {
            Long productId = entry.getKey(); // Mendapatkan kunci (productId)
            CartItem cartItem = entry.getValue(); // Mendapatkan nilai (CartItem)

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.item_belanjaan, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 8, 8, 8);  // Set margin untuk item
            itemView.setLayoutParams(layoutParams);

            TextView itemDetails = itemView.findViewById(R.id.textViewItemDetails);
            TextView itemTotal = itemView.findViewById(R.id.textViewItemTotal);
            ImageView imageViewDelete = itemView.findViewById(R.id.imageViewDelete);

            double finalPrice= Math.min(cartItem.getPrice(),Math.min(cartItem.getSpecialPrice(),cartItem.getWholeSalePrice()));



//            Toast.makeText(this,
//                    "spesial : "+cartItem.getSpecialPrice()
//                    +" , wholes : "+cartItem.getWholeSalePrice()+" , price = "+cartItem.getPrice(),Toast.LENGTH_LONG).show();

            String TipeHarga="";
            long promoId=0,promotype=0;

            if(finalPrice==cartItem.getPrice())
            {
                //TipeHarga = "Harga Normal : "+finalPrice;
                TipeHarga = "Harga Normal";
                promoId=0;
                promotype=0;
            }
            else if(finalPrice==cartItem.getSpecialPrice())
            {
//                TipeHarga = "Harga spesial : "+ cartItem.getNamaHargaSpecial()+" : "+formatRupiah(finalPrice);
                TipeHarga = "Harga spesial";
//                if(isEditInit)
//                {
//                    TipeHarga =  cartItem.getNamaHargaSpecial()+" : "+formatRupiah(finalPrice);
//                }
                promoId=cartItem.getIdSpecialPrice();
                promotype=2;
            }
            else if(finalPrice==cartItem.getWholeSalePrice())
            {
                //TipeHarga = "Harga wholesale : "+cartItem.getNamaWholesale()+" : " +formatRupiah(finalPrice);
                TipeHarga = "Harga wholesale";
                //      if(isEditInit)
//                {
//                    TipeHarga =  cartItem.getNamaWholesale()+" : "+formatRupiah(finalPrice);
//                }
                promoId=cartItem.getIdWholesale();
                promotype=1;
            }
            cartItem.setFinalPrice(finalPrice);

            double total = cartItem.getQuantity() * finalPrice;

            itemDetails.setText(cartItem.getProductName()
                    + "\n" + cartItem.getQuantity() +" "+cartItem.getUnit() + " x " + finalPrice
                    + "\n"+TipeHarga
                    + "\n"+"HPP : "+formatRupiah(cartItem.getHpp())
            );
            itemTotal.setText(formatRupiah(total));

            // Tambahkan listener untuk mengklik item
            if(!isEditMode) {
                itemView.setOnClickListener(v -> {
                    // Panggil dialog untuk mengubah kuantitas
                    showQuantityDialog(productId,cartItem.getProductName(),cartItem.getQuantity(),cartItem.getProductQuantity(),cartItem.getPrice(),cartItem.getUnit());
                });
            }

            imageViewDelete.setOnClickListener(v -> {

                cartViewModel.removeCartItem(cartItem.getProductId());

            });

            itemsContainer.addView(itemView);
            cartItem.setItemView(itemView);

            JSONObject item1 = new JSONObject();
            item1.put("product_id", cartItem.getProductId());
            item1.put("name", cartItem.getProductName() );
            item1.put("primary_price", cartItem.getHpp());
            item1.put("sell_price", cartItem.getFinalPrice());
            item1.put("qty", cartItem.getQuantity());
            item1.put("unit", cartItem.getUnit());
            item1.put("price_type", TipeHarga);
            item1.put("normal_price", cartItem.getPrice());


            // Menambahkan item pertama ke array
            itemsArray.put(item1);

            JSONObject promo1 = new JSONObject();
            promo1.put("promo_id",promoId);
            promo1.put("promo_name",TipeHarga);
            promo1.put("promo_type",promotype);
            promo1.put("promo_value",finalPrice);
            promo1.put("product_id",cartItem.getProductId());
            promoItemsArray.put(promo1);


            double itemPrice = entry.getValue().getFinalPrice()*entry.getValue().getQuantity();
            double itemHPP = entry.getValue().getHpp()*entry.getValue().getQuantity();
            subtotal += itemPrice;
            hpp += itemHPP;

        }
        saleDetailJson.put("items", itemsArray);
        saleDetailJson.put("customer_name", textViewSelectedCustomer.getText().toString());
        promoDetailJson.put("promo_detail",promoItemsArray);


            updateTotal();
        }

        catch (Exception e) {
            e.printStackTrace();
        }


    }//updateUi



    private void updateSpecialPrice(long customerGroupId) {

        // Menggunakan CountDownLatch untuk menunggu semua harga spesial diperoleh
        CountDownLatch latch = new CountDownLatch(cartItemsMap.size());

        buttonChooseCustomer.setActivated(false);

        //itemsContainer.removeAllViews();

        for (Map.Entry<Long, CartItem> entry : cartItemsMap.entrySet()) {
            Long productId = entry.getKey(); // Mendapatkan kunci (productId)
            CartItem ci = entry.getValue(); // Mendapatkan nilai (CartItem)

                 // Ambil harga grosir dan spesial untuk item baru

                getSpecialPrice(productId, customerGroupId, ci.getPrice(), (specialPrice,namaHargaSpecial,spID) -> {
                    // Perbarui harga akhir ke CartItem

                    ci.setSpecialPrice(specialPrice);
                    ci.setIdSpecialPrice(spID);
                    ci.setNamaHargaSpecial(namaHargaSpecial);
                    latch.countDown();
                    // Set harga akhir ke CartItem
                     // Tambahkan item ke keranjang
                   // Toast.makeText(this," harga spesial "+entry.getValue().getSpecialPrice(),Toast.LENGTH_SHORT).show();
                });
        }

        new Thread(() -> {
            try {
                latch.await(); // Tunggu hingga semua harga spesial diperoleh
                runOnUiThread(() -> {
                    // Memperbarui UI setelah semua harga spesial diperoleh
                   cartViewModel.setCartItemsMap(cartItemsMap); // Panggil metode untuk memperbarui UI
                    buttonChooseCustomer.setActivated(true);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    } ///akhir updatespesialprice



    private void showQuantityDialog(long productId,String NamaProduk,double qty,
                                    double productQty,double price,String unit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(new StringBuilder().append(NamaProduk).append("\n").append("Stok : ").append((productQty)).toString());


        // Buat NumberPicker untuk memilih kuantitas
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1); // Minimum kuantitas
        numberPicker.setMaxValue((int) (productQty)); // Set maksimum kuantitas sesuai kebutuhan
        numberPicker.setValue((int) qty); // Set nilai saat ini
        builder.setView(numberPicker);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int newQuantity = numberPicker.getValue();
            if (newQuantity > 0 ) {

                if (newQuantity<=productQty){
                     // Cek harga grosir dan spesial setelah mengubah kuantitas
                    getWholesalePrice(productId, newQuantity, (wholesalePrice,namaHargaGrosir,wsID) -> {
                        getSpecialPrice(productId, customerGroupId,price,(specialPrice,namaHargaSpesial,spID) -> {
                            cartViewModel.updateCartItemQuantityAndPrice(productId, newQuantity, wholesalePrice, specialPrice, namaHargaGrosir,namaHargaSpesial,spID,wsID);
                        });
                    });
//                    addItemView(productId, NamaProduk, price, productQty, newQuantity,hpp, unit);

                }
                else {

                    Toast.makeText(this, "melebihi stok", Toast.LENGTH_SHORT).show();
                }
                // Update kuantitas di keranjang

            } else {
                Toast.makeText(this, "Kuantitas tidak boleh kurang dari 1", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void loadCashOptions() {


        LiveData<List<Cash>> ldCash = cashViewModel.getAllCash();
        ldCash.observe(this, new Observer<List<Cash>>() {


            @Override
            public void onChanged(List<Cash> cashList) {

                // Populate spinner with cash options
                ArrayAdapter<Cash> adapter = new ArrayAdapter<>(PenjualanKasirActivity.this,
                        android.R.layout.simple_spinner_item, cashList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerKas.setAdapter(adapter);

                spinnerKas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCashId = cashList.get(position).getId();
                        selectedCashName = cashList.get(position).getCash_name();
                      //  Toast.makeText(PenjualanKasirActivity.this,"cash : "+selectedCashName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedCashId = 0;
                    }
                });
                ldCash.removeObserver(this);

            }
        });



    }

    private void updateShippingCost() {
        String shippingCostText = editTextShippingCostInput.getText().toString();
        ongkir = shippingCostText.isEmpty() ? 0 : Double.parseDouble(shippingCostText);
        textViewShippingCost.setText(formatRupiah(ongkir));
        updateTotal();
    }

    private void updateDiscount() {
        String discountText = editTextDiscountInput.getText().toString();
        potongan = discountText.isEmpty() ? 0 : Double.parseDouble(discountText);
        textViewDiscount.setText(formatRupiah(potongan));
        updateTotal();

    }

    private void updatePaidAmount() {
        String paidText = editTextPaid.getText().toString();
        paidAmount = paidText.isEmpty() ? 0 : Double.parseDouble(paidText);
        textViewPaidFormatted.setText(formatRupiah(paidAmount));
        updateChange(paidAmount);
    }

    private void updateChange(double paidAmount) {

        double change = paidAmount - total;
        if(change<0)
        {
            textViewLabelChange.setText("Piutang");
            paymentType=2;
        }
        else {
            paymentType=1;
            textViewLabelChange.setText("Kembalian");
        }

        textViewChange.setText(formatRupiah(change));
    }



    private void updateTotal() {
       //double subtotal = calculateSubtotal();
        textViewSubTotal.setText(formatRupiah(subtotal));

        total = subtotal + ongkir - potongan;
        textViewTotal.setText(formatRupiah(total));
        updatePaidAmount();

    }


    private String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year1, month1, dayOfMonth);
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);
            cartViewModel.setCurrentDate(selectedCalendar.getTimeInMillis());

            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
           // EditText editTextDate = findViewById(R.id.editTextDate);
           // editTextDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void addItemView(long productId, String namaBarang, double price, double productQty, double quantity, double HPP, String unit) {
        if (cartItemsMap.containsKey(productId)) {
            Toast.makeText(this, "Item sudah ada, menambah kuantitas", Toast.LENGTH_SHORT).show();
            // Update existing item view


            // Update quantity and total
          //  String[] detailsParts = itemDetails.getText().toString().split("\n")[1].split(" x ");
            double existingQty = cartItemsMap.get(productId).getQuantity();
            double newQty = existingQty + quantity;
            // Cek harga grosir dan spesial setelah mengubah kuantitas
            getWholesalePrice(productId, newQty, (wholesalePrice,wholesaleName,wsID) -> {
                getSpecialPrice(productId, customerGroupId, price, (specialPrice,specialPriceName,spID) -> {
                    // Perbarui kuantitas dan harga di keranjang
                    cartViewModel.updateCartItemQuantityAndPrice(productId, newQty, wholesalePrice, specialPrice, wholesaleName, specialPriceName,spID,wsID);

                });
            });


        } else {
            Toast.makeText(this, "Menambahkan item baru", Toast.LENGTH_SHORT).show();
            CartItem ci = new CartItem(productId, namaBarang, quantity,productQty, price, HPP,unit);

            // Ambil harga grosir dan spesial untuk item baru
            getWholesalePrice(productId, quantity, (wholesalePrice,wholsalePriceName,wsId) -> {
                getSpecialPrice(productId, customerGroupId, price, (specialPrice,specialPriceName,spId) -> {
                    // Perbarui harga akhir ke CartItem

                    // Set harga akhir ke CartItem
                    ci.setSpecialPrice(specialPrice);
                    ci.setNamaHargaSpecial(specialPriceName);
                    ci.setWholeSalePrice(wholesalePrice);
                    ci.setNamaWholesale(wholsalePriceName);
                    ci.setIdSpecialPrice(spId);
                    ci.setIdWholesale(wsId);
                    cartViewModel.addCartItem(ci); // Tambahkan item ke keranjang
                });
            });

        }



    }



    private void launchPilihProdukActivity() {
        Intent intent = new Intent(PenjualanKasirActivity.this, PilihProdukActivity.class);

        // Convert itemViewsMap to a list of product IDs and quantities
        ArrayList<String> productIdsAndQuantities = new ArrayList<>();


        for (Map.Entry<Long, CartItem> entry : cartItemsMap.entrySet()) {
            Long productId = entry.getKey();
            double quantity = entry.getValue().getQuantity();
            if(oldCartItemsMap.get(productId)!=null)
            {
                quantity-=oldCartItemsMap.get(productId).getQuantity();
            }

            productIdsAndQuantities.add(productId + ":" + quantity);
        }

        for (Map.Entry<Long, CartItem> entry : oldCartItemsMap.entrySet()) {
            Long productId = entry.getKey();
            double quantity = entry.getValue().getQuantity();
            if(cartItemsMap.get(productId)==null)
            {
                productIdsAndQuantities.add(productId + ":" + -quantity);
            }


        }



        intent.putStringArrayListExtra("productIdsAndQuantities", productIdsAndQuantities);
        pilihProdukLauncher.launch(intent);
    }


    private void getWholesalePrice(long productId, double quantity, OnPriceRetrievedListener listener) {
        // Ambil harga grosir dari database
        LiveData<Wholesale> wholesaleLiveData = wholesaleViewModel.getWholesalePriceForProduct(productId, quantity);
        wholesaleLiveData.observe(this, new Observer<Wholesale>() {
            @Override
            public void onChanged(Wholesale wholesale) {
                if (wholesale != null) {
                    listener.onPriceRetrieved(wholesale.getPrice(),wholesale.getName(),wholesale.get_id());
                } else {
                    listener.onPriceRetrieved(Double.MAX_VALUE,"",-1); // Jika tidak ada harga grosir, kembalikan 0
                }
                wholesaleLiveData.removeObserver(this); // Hapus observer setelah mendapatkan data
            }
        });
    }

    private void getSpecialPrice(long productId,long groupId, double normalPrice, OnPriceRetrievedListener listener) {
        // Ambil harga spesial dari database
        LiveData<SpecialPrice> specialPriceLiveData = specialPriceViewModel.getSpecialPriceByGroupIdAndProductId(groupId,productId);
        specialPriceLiveData.observe(this, new Observer<SpecialPrice>() {
            @Override
            public void onChanged(SpecialPrice specialPrice) {
                if (specialPrice != null) {
                    listener.onPriceRetrieved((100.0-specialPrice.getPercent())*0.01*normalPrice,specialPrice.getName(),specialPrice.get_id());
                } else {
                    listener.onPriceRetrieved(Double.MAX_VALUE,"",-1); // Jika tidak ada harga spesial, kembalikan 0
                }
                specialPriceLiveData.removeObserver(this); // Hapus observer setelah mendapatkan data
            }
        });
    }

    private void createPdfWithSAF() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Nota_Penjualan_" + editTextDate.getText().toString() + ".pdf");
        createPdfLauncher.launch(intent);
    }

    // Metode untuk berbagi PDF
    private void sharePdf(Uri uri) {
//        Log.d(TAG, "Membagikan PDF dengan URI: " + uri.toString());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getPackageManager()) != null) {
//            Log.d(TAG, "Membuka chooser untuk membagikan PDF.");
//            startActivity(Intent.createChooser(intent, "Bagikan Nota"));
//            startActivityForResult(Intent.createChooser(intent, "Bagikan Nota"), SHARE_PDF_REQUEST_CODE);
            Intent chooser = Intent.createChooser(intent, "Bagikan Nota");
            sharePdfLauncher.launch(chooser);
        } else {
//            Log.e(TAG, "Tidak ada aplikasi yang dapat menangani intent ini.");
            Toast.makeText(this, "Tidak ada aplikasi yang dapat membagikan PDF.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }




    // Listener untuk mendapatkan harga
    interface OnPriceRetrievedListener {
        void onPriceRetrieved(double price,String namaHarga,long id);
    }


}


