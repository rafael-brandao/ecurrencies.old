package ecurrencies.libertyreserve.util.impl

import static groovy.transform.PackageScopeTarget.CLASS
import static groovy.transform.PackageScopeTarget.FIELDS
import groovy.transform.PackageScope


@PackageScope([CLASS, FIELDS])
interface Constants {

    static final String TOKEN_DATE_PATTERN = "TOKEN_DATE_PATTERN"
    static final String TOKEN_DATE_TIMEZONE = "TOKEN_DATE_TIMEZONE"
    static final String TOKEN_DIGEST_ALGORITHM = "TOKEN_DIGEST_ALGORITHM"
}
