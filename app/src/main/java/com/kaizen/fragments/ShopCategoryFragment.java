package com.kaizen.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.TextView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.kaizen.R;
import com.kaizen.adapters.CartShopAdapter;
import com.kaizen.adapters.ChildCategoryPager;


import com.kaizen.adapters.ShopCategoryAdapter;
import com.kaizen.listeners.DateTimeSetListener;

import com.kaizen.listeners.ISetOnShopChildClickListerner;
import com.kaizen.models.Banner;
import com.kaizen.models.BannerResponse;
import com.kaizen.models.Category;
import com.kaizen.models.ListChildCategory;

import com.kaizen.models.RequestResponse;
import com.kaizen.models.Settings;
import com.kaizen.models.SettingsResponse;
import com.kaizen.models.ShopCategory;
import com.kaizen.models.ShopCategoryResponse;
import com.kaizen.models.ShopItem;
import com.kaizen.models.ShopItemListResponse;
import com.kaizen.models.SubcategoryResponse;
import com.kaizen.models.User;
import com.kaizen.reterofit.RetrofitInstance;
import com.kaizen.reterofit.RetrofitService;
import com.kaizen.utils.DateTimeUtil;
import com.kaizen.utils.PreferenceUtil;
import com.kaizen.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopCategoryFragment extends Fragment implements ISetOnShopChildClickListerner, View.OnClickListener {

    private static final String CATEGORY = "CATEGORY";

    private RequestOptions requestOptions;
    private RetrofitService service;
    private Category category;
    private String subCatId, childId;
    CartShopAdapter cartShopAdapter;
    RecyclerView rv_shopcata;

    public static ShopCategoryFragment newInstance(Category category) {
        ShopCategoryFragment categoryFragment = new ShopCategoryFragment();

        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY, new Gson().toJson(category));
        categoryFragment.setArguments(bundle);

        return categoryFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String value = getArguments().getString(CATEGORY);
        category = new Gson().fromJson(value, Category.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv_shopcata = view.findViewById(R.id.rv_shopcata);
        rv_shopcata.setLayoutManager(new GridLayoutManager(getContext(), 1));


        requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL);


        service = RetrofitInstance.createService(RetrofitService.class);


        service.getBanners(PreferenceUtil.getLanguage(getContext()), category.getId()).enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(Call<BannerResponse> call, Response<BannerResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    List<ListChildCategory> listChildCategorys = new ArrayList<>();

                    for (Banner banner : response.body().getReports()) {
                        ListChildCategory listChildCategory = new ListChildCategory();
                        listChildCategory.setBrandName(banner.getMainTitle());
                        listChildCategory.setDescription(banner.getSubTitle());
                        listChildCategory.setMainImage(banner.getBannerImg());
                        listChildCategory.setId(banner.getId());
                        listChildCategory.setEnquiry(banner.isEnquiry());
                        listChildCategory.setEnableClick(true);
                        listChildCategory.setCatId(banner.getCatId());
                        listChildCategory.setMainCatId(banner.getMainCatId());
                        listChildCategory.setSubCatId(banner.getSubCatId());
                        listChildCategorys.add(listChildCategory);
                    }

                    if (getActivity() != null && !getActivity().isFinishing()) {
                        ChildCategoryPager childCategoryPager = new ChildCategoryPager(getChildFragmentManager(), listChildCategorys);

                    }
                } else {
                    ToastUtil.showError(getActivity(), R.string.something_went_wrong);
                }

            }

            @Override
            public void onFailure(Call<BannerResponse> call, Throwable t) {
                ToastUtil.showError(getActivity(), R.string.something_went_wrong);
            }
        });


        RecyclerView rv_sub_category = view.findViewById(R.id.rv_sub_category);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv_sub_category.setLayoutManager(layoutManager);
        final ShopCategoryAdapter shopCategoryAdapter = new ShopCategoryAdapter(category, subCatId, childId, this);
        rv_sub_category.setAdapter(shopCategoryAdapter);

        service.getShopCategory(PreferenceUtil.getLanguage(getContext())).enqueue(new Callback<ShopCategoryResponse>() {
            @Override
            public void onResponse(Call<ShopCategoryResponse> call, Response<ShopCategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shopCategoryAdapter.addItems(response.body().getShopmaincategory());


                } else {
                    ToastUtil.showError(getActivity(), R.string.something_went_wrong);
                }
            }

            @Override
            public void onFailure(Call<ShopCategoryResponse> call, Throwable t) {
                ToastUtil.showError(getActivity(), R.string.something_went_wrong);
            }
        });

        service.getSubCategories(PreferenceUtil.getLanguage(getContext()), category.getId()).enqueue(new Callback<SubcategoryResponse>() {
            @Override
            public void onResponse(Call<SubcategoryResponse> call, Response<SubcategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                } else {
                    ToastUtil.showError(getActivity(), R.string.something_went_wrong);
                }
            }

            @Override
            public void onFailure(Call<SubcategoryResponse> call, Throwable t) {
                ToastUtil.showError(getActivity(), R.string.something_went_wrong);
            }
        });


        final TextView tv_service_time = view.findViewById(R.id.tv_service_time);
        TextView tv_feed_back = view.findViewById(R.id.tv_feed_back);
        tv_feed_back.setOnClickListener(this);
        TextView tv_collect_tray = view.findViewById(R.id.tv_collect_tray);
        tv_collect_tray.setOnClickListener(this);

        int language = PreferenceUtil.getLanguage(getContext());

        if (language == 1) {
            tv_feed_back.setText(R.string.feedback);
            tv_collect_tray.setText(R.string.collect_tray);
        } else {
            tv_feed_back.setText(R.string.feedback_arabic);
            tv_collect_tray.setText(R.string.collect_tray_arabic);
        }

        service.getSettings(PreferenceUtil.getLanguage(getContext())).enqueue(new Callback<SettingsResponse>() {
            @Override
            public void onResponse(Call<SettingsResponse> call, Response<SettingsResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    SettingsResponse settingsResponse = response.body();

                    if (settingsResponse.getSettings().size() > 0) {
                        Settings settings = settingsResponse.getSettings().get(0);

                        int language = PreferenceUtil.getLanguage(getContext());

                        if (language == 1) {
                            tv_service_time.setText(String.format(getString(R.string.service_time), settings.getName()));
                        } else {
                            tv_service_time.setText(String.format(getString(R.string.service_time_arabic), settings.getName()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SettingsResponse> call, Throwable t) {

            }
        });
    }


    public void openMenu(String subCatId, String childId) {
        this.subCatId = subCatId;
        this.childId = childId;
    }

    @Override
    public void onShopItemClick(ShopItem shopItem,final int position) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rv_shopcata.scrollToPosition(position);
                }
            }, 200);


        } catch (Exception e) {
            e.printStackTrace();
        }

        childId = null;
        subCatId = null;
    }

    @Override
    public void onSubCategoryClick(ShopCategory shopCategory) {
        service.getShopItems(PreferenceUtil.getLanguage(getContext()), shopCategory.getId()).enqueue(new Callback<ShopItemListResponse>() {
            @Override
            public void onResponse(Call<ShopItemListResponse> call, Response<ShopItemListResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        //ShopItemPager childCategoryPager = new ShopItemPager(getChildFragmentManager(), response.body().getShopitemlist());
                        cartShopAdapter = new CartShopAdapter();
                        rv_shopcata.setAdapter(cartShopAdapter);
                        cartShopAdapter.addItems(response.body().getShopitemlist());
                    }
                } else {
                    ToastUtil.showError(getActivity(), R.string.something_went_wrong);
                }
            }

            @Override
            public void onFailure(Call<ShopItemListResponse> call, Throwable t) {
                ToastUtil.showError(getActivity(), R.string.something_went_wrong);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_feed_back:
                sendFeedBack();
                break;
            case R.id.tv_collect_tray:
                collectTray();
                break;
        }
    }

    private void sendFeedBack() {
        final User user = PreferenceUtil.getUser(getContext());

        View feedBackView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);
        final EditText et_name = feedBackView.findViewById(R.id.et_name);
        final EditText et_description = feedBackView.findViewById(R.id.et_description);

        AlertDialog.Builder feedBackBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.feedback)
                .setView(feedBackView)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        String name = et_name.getText().toString().trim();
                        String description = et_description.getText().toString().trim();

                        if (name.isEmpty()) {
                            ToastUtil.showError(getActivity(), R.string.enter_name);
                        } else if (description.isEmpty()) {
                            ToastUtil.showError(getActivity(), R.string.enter_description);
                        } else {
                            service.sendFeedBack(PreferenceUtil.getLanguage(getContext()), user.getRoomno(), name, description).enqueue(new Callback<RequestResponse>() {
                                @Override
                                public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        RequestResponse requestResponse = response.body();

                                        if (requestResponse.isResponce()) {
                                            ToastUtil.showSuccess(getActivity(), requestResponse.getMessage());
                                            dialog.dismiss();
                                        } else {
                                            ToastUtil.showError(getActivity(), requestResponse.getError());
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<RequestResponse> call, Throwable t) {

                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        feedBackBuilder.create().show();
    }

    private void collectTray() {
        final User user = PreferenceUtil.getUser(getContext());
        View collectTrayView = getLayoutInflater().inflate(R.layout.dialog_collect_tray, null);
        final EditText et_name = collectTrayView.findViewById(R.id.et_name);
        final EditText et_time = collectTrayView.findViewById(R.id.et_time);

        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DateTimeUtil().datePicker(getContext(), new DateTimeSetListener() {
                    @Override
                    public void onDateTimeSet(String date) {
                        et_time.setText(date);
                    }
                });
            }
        });

        AlertDialog.Builder collectTrayBuilder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.collect_tray)
                .setView(collectTrayView)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        String name = et_name.getText().toString().trim();
                        String timing = et_time.getText().toString().trim();

                        if (name.isEmpty()) {
                            ToastUtil.showError(getActivity(), R.string.enter_name);
                        } else if (timing.isEmpty()) {
                            ToastUtil.showError(getActivity(), R.string.enter_date_time);
                        } else {
                            service.collectTray(PreferenceUtil.getLanguage(getContext()), user.getRoomno(), name, timing).enqueue(new Callback<RequestResponse>() {
                                @Override
                                public void onResponse(Call<RequestResponse> call, Response<RequestResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        RequestResponse requestResponse = response.body();

                                        if (requestResponse.isResponce()) {
                                            ToastUtil.showSuccess(getActivity(), requestResponse.getMessage());
                                            dialog.dismiss();
                                        } else {
                                            ToastUtil.showError(getActivity(), requestResponse.getError());
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<RequestResponse> call, Throwable t) {

                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        collectTrayBuilder.create().show();
    }
}

