package com.loic.common.manager;

import com.loic.common.manager.impl.CalendarManagerImpl;
import com.loic.common.manager.impl.LoadImgManagerImpl;

public class ManagerUtilsFactory
{
    public static enum ManagerTypeEnum
    {
        Load_Image_Manager (LoadImgManagerImpl.class),
        Calendar_Manager (CalendarManagerImpl.class);

        private Class<? extends BasicManager> attachManagerClass;

        private ManagerTypeEnum(Class<? extends BasicManager> attachManagerClass)
        {
            this.attachManagerClass = attachManagerClass;
        }
    }
    
    private static final BasicManager[] availableManagers = new BasicManager[ManagerTypeEnum.values().length];

    public static BasicManager getUtilsManager (ManagerTypeEnum managerTypeEnum)
    {
        int managerIndex = managerTypeEnum == null ? -1 : managerTypeEnum.ordinal();
        if(managerIndex >= 0 && managerIndex < availableManagers.length)
        {
            if(availableManagers[managerIndex] == null)
            {
                try
                {
                    availableManagers[managerIndex] = managerTypeEnum.attachManagerClass.newInstance();
                } catch (InstantiationException e)
                {
                    e.printStackTrace();
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
            return availableManagers[managerIndex];
        }
        return null;
    }
}
