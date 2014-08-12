package com.code44.finance.api.currencies;

import android.content.Context;

import com.code44.finance.App;
import com.code44.finance.BuildConfig;
import com.code44.finance.api.DefaultNetworkExecutor;
import com.code44.finance.api.NetworkExecutor;
import com.code44.finance.api.Request;
import com.code44.finance.common.utils.Preconditions;
import com.code44.finance.data.db.model.Currency;
import com.code44.finance.utils.EventBus;

import java.util.List;

import retrofit.RestAdapter;

public class CurrenciesApi {
    private static CurrenciesApi singleton;

    private final NetworkExecutor executor;
    private final Context context;
    private final EventBus eventBus;
    private final CurrenciesRequestService requestService;

    public CurrenciesApi(NetworkExecutor executor, Context context, EventBus eventBus, CurrenciesRequestService requestService) {
        Preconditions.checkNotNull(executor, "Executor cannot be null.");
        Preconditions.checkNotNull(context, "Context cannot be null.");
        Preconditions.checkNotNull(eventBus, "EventBus cannot be null.");
        Preconditions.checkNotNull(requestService, "CurrenciesRequestService cannot be null.");

        this.executor = executor;
        this.context = context;
        this.eventBus = eventBus;
        this.requestService = requestService;
    }

    public static synchronized CurrenciesApi get() {
        if (singleton == null) {
            final NetworkExecutor networkExecutor = DefaultNetworkExecutor.get();
            final Context context = App.getContext();
            final EventBus eventBus = EventBus.get();
            final String endpoint = "http://rate-exchange.appspot.com";
            final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build();
            restAdapter.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);
            final CurrenciesRequestService requestService = restAdapter.create(CurrenciesRequestService.class);
            singleton = new CurrenciesApi(networkExecutor, context, eventBus, requestService);
        }
        return singleton;
    }

    public void updateExchangeRate(String fromCode) {
        final String toCode = Currency.getDefault().getCode();
        final ExchangeRateRequest request = new ExchangeRateRequest(eventBus, context, requestService, fromCode, toCode, true);
        if (!isWorking(request)) {
            executeRequest(request);
        }
    }

    public void updateExchangeRates(List<String> fromCodes) {
        final String toCode = Currency.getDefault().getCode();
        final ExchangeRatesRequest request = new ExchangeRatesRequest(eventBus, context, requestService, fromCodes, toCode);
        if (!isWorking(request)) {
            executeRequest(request);
        }
    }

    public boolean isWorking(Request request) {
        return executor.isWorking(request);
    }

    private void executeRequest(Request request) {
        executor.execute(request);
    }
}
