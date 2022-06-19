package com.andb.apps.weather.data.repository

import com.andb.apps.weather.data.model.Conditions

interface ProviderRepo {
    fun fillConditions(conditions: Conditions): Conditions
}