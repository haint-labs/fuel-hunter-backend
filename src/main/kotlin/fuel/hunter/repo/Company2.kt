package fuel.hunter.repo

import fuel.hunter.models.Company

data class Description(
    val en: String,
    val lv: String,
    val ru: String,
    val lg: String,
)

data class Logo(
    val x2: String,
    val x3: String,
)

data class Company2(
    val name: String,
    val description: Description,
    val homepage: String,
    val logo: Logo,
    val largeLogo: Logo,
    val mapLogo: Logo,
    val mapGrayLogo: Logo,
)

fun Company.toEntity(): Company2 =
    Company2(
        name,
        Description(description.en, description.lv, description.ru, description.lg),
        homepage,
        logo = Logo(logo.x2, logo.x3),
        largeLogo = Logo(largeLogo.x2, largeLogo.x3),
        mapLogo = Logo(mapLogo.x2, mapLogo.x3),
        mapGrayLogo = Logo(mapGreyLogo.x2, mapGreyLogo.x3)
    )

fun Company2.fromEntity(): Company =
    Company.newBuilder()
        .setName(name)
        .setDescription(
            Company.Description.newBuilder()
                .setEn(description.en)
                .setLv(description.lv)
                .setRu(description.ru)
                .setLg(description.lg)
                .build()
        )
        .setHomepage(homepage)
        .setLogo(
            Company.Logo.newBuilder()
                .setX2(logo.x2)
                .setX3(logo.x3)
                .build()
        )
        .setLargeLogo(
            Company.Logo.newBuilder()
                .setX2(largeLogo.x2)
                .setX3(largeLogo.x3)
                .build()
        )
        .setMapLogo(
            Company.Logo.newBuilder()
                .setX2(mapLogo.x2)
                .setX3(mapLogo.x3)
                .build()
        )
        .setMapGreyLogo(
            Company.Logo.newBuilder()
                .setX2(mapGrayLogo.x2)
                .setX3(mapGrayLogo.x3)
                .build()
        )
        .build()