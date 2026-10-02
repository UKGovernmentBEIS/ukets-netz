package uk.gov.netz.api.notification.mail.files;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

final class PublicFileUrlFactory {

    private final String baseUrl;
    private final String authority;

    PublicFileUrlFactory(URI publicBaseUrl) {
        this.authority = publicBaseUrl.getRawAuthority();
        this.baseUrl = publicBaseUrl.getScheme().toLowerCase(Locale.ROOT) + "://"
                + authority + stripTrailingSlashes(publicBaseUrl.getRawPath());
    }

    URI create(String path) {
        try {
            String encodedPath = new URI(null, authority, "/" + path, null, null).getRawPath();
            return new URI(baseUrl + encodedPath);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("notification.email-link configuration cannot produce a valid public URL", exception);
        }
    }

    private static String stripTrailingSlashes(String basePath) {
        String path = basePath == null ? "" : basePath;
        int end = path.length();
        while (end > 0 && path.charAt(end - 1) == '/') {
            end--;
        }
        return path.substring(0, end);
    }

}
