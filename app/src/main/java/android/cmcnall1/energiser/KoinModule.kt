package android.cmcnall1.energiser

import org.koin.dsl.module.module

/*
    Koin module used for dependency injection
 */

val koin = module {
    factory {
        MainPresenter(
            get()
        )
    }
    single{
        ChargePointRepo(
            get(),
            get()
        )
    }
    single{
        ChargePointService()
    }
    single {
        NearbyPlacesService()
    }
    single{
        NearbyPlacesRepo(
            get()
        )
    }
    single{
        FilterService()
    }
    factory {
        DetailPresenter(
            get()
        )
    }
}