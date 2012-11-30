package ecurrencies.libertyreserve.transformer.api;

import com.google.protobuf.Message;

public interface ConverterFactory {

    public static final String TYPE = "type";
    public static final String CONTENT_TYPE = "contentType";

    <Request extends Message> RequestConverter<Request> requestConverter(
            final Class<Request> requestClass);

    <Response extends Message> ResponseConverter<Response> responseConverter(
            final Class<Response> responseClass);

}
