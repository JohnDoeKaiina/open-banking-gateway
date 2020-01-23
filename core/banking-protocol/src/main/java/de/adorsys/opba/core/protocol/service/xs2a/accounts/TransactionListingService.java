package de.adorsys.opba.core.protocol.service.xs2a.accounts;

import de.adorsys.opba.core.protocol.service.ContextUtil;
import de.adorsys.opba.core.protocol.service.ValidatedExecution;
import de.adorsys.opba.core.protocol.service.dto.ValidatedPathQueryHeaders;
import de.adorsys.opba.core.protocol.service.mapper.PathQueryHeadersMapperTemplate;
import de.adorsys.opba.core.protocol.service.xs2a.context.TransactionListXs2aContext;
import de.adorsys.opba.core.protocol.service.xs2a.context.Xs2aContext;
import de.adorsys.opba.core.protocol.service.xs2a.dto.DtoMapper;
import de.adorsys.opba.core.protocol.service.xs2a.dto.Xs2aResourceParameters;
import de.adorsys.opba.core.protocol.service.xs2a.dto.Xs2aStandardHeaders;
import de.adorsys.opba.core.protocol.service.xs2a.dto.Xs2aTransactionParameters;
import de.adorsys.opba.core.protocol.service.xs2a.validation.Xs2aValidator;
import de.adorsys.xs2a.adapter.service.AccountInformationService;
import de.adorsys.xs2a.adapter.service.Response;
import de.adorsys.xs2a.adapter.service.model.TransactionsReport;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service("xs2aTransactionListing")
@RequiredArgsConstructor
public class TransactionListingService extends ValidatedExecution<TransactionListXs2aContext> {

    private final Extractor extractor;
    private final Xs2aValidator validator;
    private final AccountInformationService ais;

    @Override
    protected void doValidate(DelegateExecution execution, TransactionListXs2aContext context) {
        validator.validate(execution, extractor.forValidation(context));
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber") // Hardcoded as it is POC, these should be read from context
    protected void doRealExecution(DelegateExecution execution, TransactionListXs2aContext context) {
        ValidatedPathQueryHeaders<Xs2aResourceParameters, Xs2aTransactionParameters, Xs2aStandardHeaders> params =
                extractor.forExecution(context);

        Response<TransactionsReport> accounts = ais.getTransactionList(
                params.getPath().getResourceId(),
                params.getHeaders().toHeaders(),
                params.getQuery().toParameters()
        );

        ContextUtil.setResult(execution, accounts.getBody());
    }

    @Override
    protected void doMockedExecution(DelegateExecution execution, TransactionListXs2aContext context) {
        ContextUtil.setResult(execution, new TransactionsReport());
    }

    @Service
    public static class Extractor extends PathQueryHeadersMapperTemplate<
                    Xs2aContext,
                    Xs2aResourceParameters,
                    Xs2aTransactionParameters,
                    Xs2aStandardHeaders> {

        public Extractor(
                DtoMapper<Xs2aContext, Xs2aStandardHeaders> toHeaders,
                DtoMapper<Xs2aContext, Xs2aResourceParameters> toPath,
                DtoMapper<Xs2aContext, Xs2aTransactionParameters> toQuery) {
            super(toHeaders, toPath, toQuery);
        }
    }
}
