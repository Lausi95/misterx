package net.lausi95.citygame.application.usecase.game.updategame

import net.lausi95.citygame.domain.Tenant
import org.springframework.stereotype.Service

@Service
class UpdateGameUseCase {

    operator fun invoke(command: UpdateGameCommand, tenant: Tenant) {
    }
}